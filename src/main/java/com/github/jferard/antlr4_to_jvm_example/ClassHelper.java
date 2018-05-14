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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

/**
 * A helper to create a class
 */
class ClassHelper {
    private final ClassWriter classWriter;
    private final String className;
    private final String targetDir;

    /**
     * @param className the name of the class (without .class)
     * @param targetDir the target directory
     */
    public ClassHelper(String className, String targetDir) {
        this.className = className;
        this.targetDir = targetDir;
        classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classWriter
                .visit(V1_8, ACC_PUBLIC, className, null, Type.getInternalName(Object.class), null);
        this.createConstructor();
        this.createMain();
    }

    private void createMain() {
        MethodVisitor main = classWriter
                .visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        // put System.out on the stack
        main.visitFieldInsn(GETSTATIC, Type.getInternalName(System.class), "out",
                Type.getDescriptor(PrintStream.class));
        // put the result of the get method on the stack
        main.visitMethodInsn(INVOKESTATIC, className, "get", "()D", false);
        // print the result of get
        main.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(PrintStream.class), "println",
                "(D)V", false);
        main.visitInsn(RETURN);
        main.visitMaxs(2, 2);
        main.visitEnd();
    }

    /**
     * All instructions will go in the static get method
     * @return the method visitor
     */
    public MethodVisitor beginGet() {
        return classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "get", "()D", null, null);
    }

    /**
     * @param get the static method
     */
    public void endGet(MethodVisitor get) {
        get.visitInsn(DRETURN);
        get.visitMaxs(2, 2);
        get.visitEnd();
    }

    /**
     * Write the class
     * @throws IOException
     */
    public void write() throws IOException {
        try (OutputStream f = new FileOutputStream(new File(targetDir, className + ".class"))) {
            f.write(classWriter.toByteArray());
        }
    }

    private void createConstructor() {
        MethodVisitor constructor = classWriter
                .visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        // put this on the stack
        constructor.visitVarInsn(ALOAD, 0);
        constructor
                .visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V",
                        false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
    }
}