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

import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TinyCalculatorCompilerTest {
    @Test
    public void test1() throws Exception {
        new TinyCalculatorCompiler("TC1", "target/classes").compile("1+(3*12)*(5-6)");
        checkResult("TC1", -35.0);
    }

    @Test
    public void test2() throws Exception {
        new TinyCalculatorCompiler("TC2", "target/classes").compile("(1+2+3+4+5+6)*(6-1-2-3)");
        checkResult("TC2", 0.0);
    }

    @Test(expected = ParseCancellationException.class)
    public void testParseError() throws Exception {
        new TinyCalculatorCompiler("TC3", "target/classes").compile("1*");
    }

    @Test(expected = LexerNoViableAltException.class)
    public void testLexError() throws Exception {
        new TinyCalculatorCompiler("TC4", "target/classes").compile("|");
    }

    private void checkResult(String className,
                             double expected) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> tc = Class.forName(className);
        Method m = tc.getMethod("get");
        Assert.assertEquals(expected, (double) m.invoke(null), 0.01);
    }
}