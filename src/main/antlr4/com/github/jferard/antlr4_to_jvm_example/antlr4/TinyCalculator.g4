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

grammar TinyCalculator;

expression
    : a=additiveExpression
    ;

additiveExpression
    : m=multiplicativeExpression # additiveOne
    | a=additiveExpression op=(PLUS|MINUS) m=multiplicativeExpression # additiveMany
    ;

multiplicativeExpression
    : p=primaryExpression # multiplicativeOne
    | m=multiplicativeExpression op=(TIMES|DIVIDE) p=primaryExpression # multiplicativeMany
    ;

primaryExpression
    : p=DOUBLE # primaryAtomic
    | L_PAREN e=expression R_PAREN # primaryBlock
    ;

// Operators
PLUS: '+';
MINUS: '-';
TIMES: '*';
DIVIDE: '/';

// Parentheses
L_PAREN: '(';
R_PAREN: ')';

fragment
DIGIT
    :    ('0'..'9');

DOUBLE
    :   DIGIT+ ('.' DIGIT+)?;

WS
    :   (' ' | '\t' | '\r'| '\n') -> channel(HIDDEN)
    ;