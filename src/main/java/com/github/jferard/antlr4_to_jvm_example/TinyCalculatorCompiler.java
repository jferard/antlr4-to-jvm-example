/*
 * Antlr4 To JVM Example - an example of JVM bytecode generator from a Antlr4
 *    simple grammar and an arithmetic expression.
 *    Copyright (C) 2018 J. Férard <https://github.com/jferard>
 *
 * This file is part of Antlr4 To JVM Example.
 *
 * Antlr4 To JVM Example is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Antlr4 To JVM Example is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.jferard.antlr4_to_jvm_example;

import com.github.jferard.antlr4_to_jvm_example.antlr4.TinyCalculatorBaseListener;
import com.github.jferard.antlr4_to_jvm_example.antlr4.TinyCalculatorLexer;
import com.github.jferard.antlr4_to_jvm_example.antlr4.TinyCalculatorParser;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;

import static org.objectweb.asm.Opcodes.DADD;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DMUL;
import static org.objectweb.asm.Opcodes.DSUB;

/**
 * A compiler for the TinyCalculator grammar. Takes a computation and creates a class.
 */
public class TinyCalculatorCompiler {
    private final String className;
    private final String targetDir;

    /**
     * @param className the name of the class (without .class)
     * @param targetDir the target directory
     */
    public TinyCalculatorCompiler(final String className, final String targetDir) {
        this.className = className;
        this.targetDir = targetDir;
    }

    /**
     * @param computation the computation (arithmetic expression)
     * @throws IOException
     */
    public void compile(final String computation) throws IOException {
        TinyCalculatorParser parser = this.parse(computation);
        ClassHelper classHelper = new ClassHelper(this.className, this.targetDir);
        MethodVisitor get = classHelper.beginGet();
        ExpressionListener expressionListener = new ExpressionListener(get);
        parser.expression().enterRule(expressionListener);
        classHelper.endGet(get);
        classHelper.write();
    }

    private TinyCalculatorParser parse(String computation) {
        CharStream charStream = CharStreams.fromString(computation);
        TinyCalculatorLexer lexer = new TinyCalculatorLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s,
                                    RecognitionException e) {
                throw e;

            }
        });
        TokenStream tokens = new CommonTokenStream(lexer);
        TinyCalculatorParser parser = new TinyCalculatorParser(tokens);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    private class ExpressionListener extends TinyCalculatorBaseListener {

        private final MethodVisitor get;

        public ExpressionListener(final MethodVisitor get) {
            this.get = get;
        }

        @Override
        public void enterExpression(final TinyCalculatorParser.ExpressionContext ctx) {
            AdditiveExpressionListener additiveExpressionListener = new AdditiveExpressionListener(
                    get);
            ctx.a.enterRule(additiveExpressionListener);
        }

    }

    private class AdditiveExpressionListener extends TinyCalculatorBaseListener {
        private final MethodVisitor get;

        public AdditiveExpressionListener(final MethodVisitor get) {
            this.get = get;
        }

        @Override
        public void enterAdditiveOne(final TinyCalculatorParser.AdditiveOneContext ctx) {
            MultiplicativeExpressionListener multiplicativeExpressionListener = new
                    MultiplicativeExpressionListener(
                    get);
            ctx.m.enterRule(multiplicativeExpressionListener);
        }

        @Override
        public void enterAdditiveMany(final TinyCalculatorParser.AdditiveManyContext ctx) {
            AdditiveExpressionListener additiveExpressionListener = new AdditiveExpressionListener(
                    get);
            MultiplicativeExpressionListener multiplicativeExpressionListener = new
                    MultiplicativeExpressionListener(
                    get);

            ctx.a.enterRule(additiveExpressionListener);
            ctx.m.enterRule(multiplicativeExpressionListener);
            if (ctx.op.getType() == TinyCalculatorParser.PLUS) {
                get.visitInsn(DADD);
            } else {
                get.visitInsn(DSUB);
            }
        }
    }

    private class MultiplicativeExpressionListener extends TinyCalculatorBaseListener {
        private final MethodVisitor get;

        public MultiplicativeExpressionListener(final MethodVisitor get) {
            this.get = get;
        }

        @Override
        public void enterMultiplicativeOne(
                final TinyCalculatorParser.MultiplicativeOneContext ctx) {
            PrimaryExpressionListener primaryExpressionListener = new PrimaryExpressionListener(
                    get);
            ctx.p.enterRule(primaryExpressionListener);
        }

        @Override
        public void enterMultiplicativeMany(
                final TinyCalculatorParser.MultiplicativeManyContext ctx) {
            MultiplicativeExpressionListener multiplicativeExpressionListener = new
                    MultiplicativeExpressionListener(
                    get);
            PrimaryExpressionListener primaryExpressionListener = new PrimaryExpressionListener(
                    get);
            ctx.m.enterRule(multiplicativeExpressionListener);
            ctx.p.enterRule(primaryExpressionListener);
            if (ctx.op.getType() == TinyCalculatorParser.TIMES) {
                get.visitInsn(DMUL);
            } else {
                get.visitInsn(DDIV);
            }
        }

    }

    private class PrimaryExpressionListener extends TinyCalculatorBaseListener {
        private final MethodVisitor get;

        PrimaryExpressionListener(final MethodVisitor get) {
            this.get = get;

        }

        @Override
        public void enterPrimaryAtomic(final TinyCalculatorParser.PrimaryAtomicContext ctx) {
            this.get.visitLdcInsn(new Double(ctx.p.getText()));
        }

        @Override
        public void enterPrimaryBlock(final TinyCalculatorParser.PrimaryBlockContext ctx) {
            ExpressionListener ExpressionListener = new ExpressionListener(get);
            ctx.e.enterRule(ExpressionListener);
        }
    }
}
