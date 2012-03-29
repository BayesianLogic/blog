/*
 * Copyright (c) 2005, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.  
 *
 * * Neither the name of the University of California, Berkeley nor
 *   the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior 
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package blog.parse;

import java.lang.*;
import java.util.*;
import java.io.*;

import java_cup.runtime.Symbol;

public class ParserUtilities {

	/**
	 * Prints an appropriately escaped string
	 * 
	 * @param str
	 *          the output stream
	 * @param s
	 *          the string to print
	 * */
	public static void printEscapedString(PrintStream str, String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\\':
				str.print("\\\\");
				break;
			case '\"':
				str.print("\\\"");
				break;
			case '\n':
				str.print("\\n");
				break;
			case '\t':
				str.print("\\t");
				break;
			case '\b':
				str.print("\\b");
				break;
			case '\f':
				str.print("\\f");
				break;
			default:
				if (c >= 0x20 && c <= 0x7f) {
					str.print(c);
				} else {
					String octal = Integer.toOctalString(c);
					str.print('\\');
					switch (octal.length()) {
					case 1:
						str.print('0');
					case 2:
						str.print('0');
					default:
						str.print(octal);
					}
				}
			}
		}
	}

	/**
	 * Returns a string representation for a token
	 * 
	 * @param s
	 *          the token
	 * @return the string representation
	 * */
	public static String tokenToString(Symbol s) {
		switch (s.sym) {
		case BLOGTokenConstants.TYPE:
			return ("TYPE");
		case BLOGTokenConstants.GUARANTEED:
			return ("GUARANTEED");
		case BLOGTokenConstants.RANDOM:
			return ("RANDOM");
		case BLOGTokenConstants.NONRANDOM:
			return ("NONRANDOM");
		case BLOGTokenConstants.ELSE:
			return ("ELSE");
		case BLOGTokenConstants.IF:
			return ("IF");
		case BLOGTokenConstants.FOR:
			return ("FOR");
		case BLOGTokenConstants.QUERY:
			return ("QUERY");
		case BLOGTokenConstants.OBS:
			return ("OBS");
		case BLOGTokenConstants.ELSEIF:
			return ("ELSEIF");
		case BLOGTokenConstants.THEN:
			return ("THEN");
		case BLOGTokenConstants.STR_CONST:
			return ("STR_CONST");
		case BLOGTokenConstants.CHAR_CONST:
			return ("CHAR_CONST");
		case BLOGTokenConstants.INT_CONST:
			return ("INT_CONST");
		case BLOGTokenConstants.DOUBLE_CONST:
			return ("DOUBLE_CONST");
		case BLOGTokenConstants.ID:
			return ("ID");
		case BLOGTokenConstants.FALSE:
			return ("FALSE");
		case BLOGTokenConstants.TRUE:
			return ("TRUE");
		case BLOGTokenConstants.NUMSIGN:
			return ("'#'");
		case BLOGTokenConstants.ERROR:
			return ("ERROR");
		case BLOGTokenConstants.error:
			return ("ERROR");
		case BLOGTokenConstants.RIGHTARROW:
			return ("'->'");
		case BLOGTokenConstants.EQ:
			return ("'='");
		case BLOGTokenConstants.NEQ:
			return ("'!='");
		case BLOGTokenConstants.NEG:
			return ("'!'");
		case BLOGTokenConstants.COMMA:
			return ("','");
		case BLOGTokenConstants.SEMI:
			return ("';'");
		case BLOGTokenConstants.COLON:
			return ("':'");
		case BLOGTokenConstants.LPAREN:
			return ("'('");
		case BLOGTokenConstants.RPAREN:
			return ("')'");
		case BLOGTokenConstants.OR:
			return ("'|'");
		case BLOGTokenConstants.AND:
			return ("'&'");
		case BLOGTokenConstants.DISTRIB:
			return ("'~'");
		case BLOGTokenConstants.LBRACE:
			return ("'{'");
		case BLOGTokenConstants.RBRACE:
			return ("'}'");
		case BLOGTokenConstants.LBRACKET:
			return ("'['");
		case BLOGTokenConstants.RBRACKET:
			return ("']'");
		case BLOGTokenConstants.LT:
			return ("'<'");
		case BLOGTokenConstants.GT:
			return ("'>'");
		case BLOGTokenConstants.LEQ:
			return ("'<='");
		case BLOGTokenConstants.GEQ:
			return ("'>='");
		case BLOGTokenConstants.EOF:
			return ("EOF");
		case BLOGTokenConstants.FORALL:
			return ("FORALL");
		case BLOGTokenConstants.EXISTS:
			return ("EXISTS");
		case BLOGTokenConstants.CLASS_NAME:
			return ("CLASS_NAME");
		case BLOGTokenConstants.GENERATING:
			return ("GENERATING");
		default:
			return ("<Invalid Token: " + s.sym + ">");
		}
	}

	/**
	 * Prints a token to stderr
	 * 
	 * @param s
	 *          the token
	 * */
	public static void printToken(Symbol s) {
		System.err.print(tokenToString(s));

		String val = null;

		switch (s.sym) {
		case BLOGTokenConstants.INT_CONST:
			val = (String) s.value;
			System.err.print(" = " + val);
			break;
		case BLOGTokenConstants.DOUBLE_CONST:
			val = (String) s.value;
			System.err.print(" = " + val);
			break;
		case BLOGTokenConstants.ID:
			val = (String) s.value;
			System.err.print(" = " + val);
			break;
		case BLOGTokenConstants.ERROR:
			System.err.print(" = \"");
			printEscapedString(System.err, s.value.toString());
			System.err.print("\"");
			break;
		}
		System.err.println("");
	}

}
