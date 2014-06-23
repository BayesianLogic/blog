/* The following code was generated by JFlex 1.5.1 */

/**
 * Copyright (c) 2005, 2012 Regents of the University of California
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
 
/**
 * Using JFlex-1.5.1
 * @author leili
 */ 
package blog.parse;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.5.1
 * from the specification file <tt>src/blog/parse/BLOGLexer.flex</tt>
 */
public class BLOGLexer implements java_cup.runtime.Scanner {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int STR_LIT = 2;
  public static final int CHAR_LIT = 4;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2, 2
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\7\1\6\1\102\1\103\1\6\22\0\1\7\1\61\1\15"+
    "\1\77\1\0\1\54\1\62\1\16\1\65\1\66\1\14\1\53\1\75"+
    "\1\5\1\3\1\13\4\11\4\10\2\2\1\74\1\73\1\56\1\60"+
    "\1\57\1\0\1\64\1\23\1\12\1\36\1\25\1\4\1\30\1\33"+
    "\1\37\1\31\2\1\1\40\1\27\1\24\1\26\1\21\1\41\1\22"+
    "\1\35\1\17\1\34\2\1\1\32\1\20\1\1\1\71\1\100\1\72"+
    "\1\55\1\1\1\0\1\47\1\101\1\36\1\25\1\45\1\46\1\33"+
    "\1\37\1\31\2\1\1\50\1\27\1\52\1\26\1\21\1\41\1\43"+
    "\1\51\1\42\1\44\2\1\1\32\1\20\1\1\1\70\1\63\1\67"+
    "\1\76\6\0\1\102\u1fa2\0\1\102\1\102\udfd6\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\3\0\1\1\1\2\1\3\1\4\1\2\1\5\1\6"+
    "\1\7\1\10\1\11\1\12\20\2\1\13\1\14\1\15"+
    "\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25"+
    "\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35"+
    "\1\36\1\37\1\40\1\41\1\42\1\40\1\43\1\44"+
    "\1\0\2\2\1\45\1\46\1\0\13\2\1\47\10\2"+
    "\1\50\1\51\1\52\1\53\1\54\1\55\2\56\1\57"+
    "\1\60\1\61\1\62\1\55\1\63\1\64\1\65\1\66"+
    "\1\44\1\0\2\2\1\46\1\0\6\2\1\67\1\2"+
    "\1\70\1\71\11\2\1\56\1\0\1\2\1\72\1\73"+
    "\1\74\11\2\1\75\1\76\1\2\1\77\1\2\1\100"+
    "\1\0\1\2\1\101\6\2\1\102\2\2\1\103\1\104"+
    "\1\0\1\105\1\106\3\2\1\107\1\110\2\2\1\111"+
    "\7\2\1\112\7\2\1\113";

  private static int [] zzUnpackAction() {
    int [] result = new int[187];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\104\0\210\0\314\0\u0110\0\u0154\0\u0198\0\u01dc"+
    "\0\u0220\0\314\0\u0264\0\314\0\314\0\314\0\u02a8\0\u02ec"+
    "\0\u0330\0\u0374\0\u03b8\0\u03fc\0\u0440\0\u0484\0\u04c8\0\u050c"+
    "\0\u0550\0\u0594\0\u05d8\0\u061c\0\u0660\0\u06a4\0\314\0\314"+
    "\0\314\0\u06e8\0\u072c\0\u0770\0\u07b4\0\314\0\314\0\314"+
    "\0\314\0\314\0\314\0\314\0\314\0\314\0\314\0\314"+
    "\0\314\0\314\0\314\0\314\0\314\0\314\0\u07f8\0\314"+
    "\0\u083c\0\u0880\0\u08c4\0\u0908\0\314\0\u094c\0\u0990\0\u09d4"+
    "\0\u0a18\0\u0a5c\0\u0aa0\0\u0ae4\0\u0b28\0\u0b6c\0\u0bb0\0\u0bf4"+
    "\0\u0c38\0\u0c7c\0\u0110\0\u0cc0\0\u0d04\0\u0d48\0\u0d8c\0\u0dd0"+
    "\0\u0e14\0\u0e58\0\u0e9c\0\314\0\314\0\314\0\314\0\314"+
    "\0\314\0\u0ee0\0\u0f24\0\314\0\314\0\314\0\314\0\u0f68"+
    "\0\314\0\314\0\314\0\314\0\u0fac\0\u0fac\0\u0ff0\0\u1034"+
    "\0\314\0\u1078\0\u10bc\0\u1100\0\u1144\0\u1188\0\u11cc\0\u1210"+
    "\0\u0110\0\u1254\0\u0110\0\u1298\0\u12dc\0\u1320\0\u1364\0\u13a8"+
    "\0\u13ec\0\u1430\0\u1474\0\u14b8\0\u14fc\0\314\0\u1540\0\u1584"+
    "\0\u0110\0\u0110\0\u0110\0\u15c8\0\u160c\0\u1650\0\u1694\0\u16d8"+
    "\0\u171c\0\u1760\0\u17a4\0\u17e8\0\u0110\0\u0110\0\u182c\0\u0110"+
    "\0\u1870\0\u0110\0\u18b4\0\u18f8\0\u0110\0\u193c\0\u1980\0\u19c4"+
    "\0\u1a08\0\u1a4c\0\u1a90\0\u0110\0\u1ad4\0\u1b18\0\u0110\0\u0110"+
    "\0\u1b5c\0\u0110\0\u0110\0\u1ba0\0\u1be4\0\u1c28\0\u0110\0\u0110"+
    "\0\u1c6c\0\u1cb0\0\314\0\u1cf4\0\u1d38\0\u1d7c\0\u1dc0\0\u1e04"+
    "\0\u1e48\0\u1e8c\0\u0110\0\u1ed0\0\u1f14\0\u1f58\0\u1f9c\0\u1fe0"+
    "\0\u2024\0\u2068\0\u0110";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[187];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\4\1\5\1\6\1\7\1\10\1\11\2\12\2\6"+
    "\1\5\1\13\1\14\1\15\1\16\1\17\1\5\1\20"+
    "\1\21\1\5\1\22\1\23\1\24\1\25\1\26\1\27"+
    "\1\5\1\30\2\5\1\31\1\5\1\32\1\33\1\34"+
    "\1\21\1\5\1\10\1\35\1\5\1\32\1\5\1\36"+
    "\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46"+
    "\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56"+
    "\1\57\1\60\1\61\1\62\1\63\1\4\1\5\1\0"+
    "\1\12\6\64\1\65\6\64\1\66\62\64\1\67\1\64"+
    "\2\0\6\64\1\65\7\64\1\70\61\64\1\67\1\64"+
    "\107\0\2\5\1\0\1\5\3\0\3\5\4\0\34\5"+
    "\26\0\1\5\4\0\1\6\1\71\1\72\3\0\2\6"+
    "\33\0\1\72\40\0\1\71\5\0\2\71\73\0\2\5"+
    "\1\0\1\5\3\0\3\5\4\0\13\5\1\73\5\5"+
    "\1\74\7\5\1\74\2\5\26\0\1\5\61\0\1\75"+
    "\37\0\1\76\1\77\70\0\2\5\1\0\1\5\3\0"+
    "\3\5\4\0\1\5\1\100\16\5\1\101\13\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\4\5\1\102\23\5\1\102\3\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\4\5\1\103"+
    "\23\5\1\103\3\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\7\5\1\104\24\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\12\5\1\105\21\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\2\5\1\106\4\0\3\5\1\107\20\5"+
    "\1\107\7\5\26\0\1\106\3\0\2\5\1\0\1\5"+
    "\3\0\3\5\4\0\4\5\1\110\23\5\1\110\3\5"+
    "\26\0\1\5\3\0\2\5\1\0\1\5\3\0\3\5"+
    "\4\0\7\5\1\111\2\5\1\112\21\5\26\0\1\5"+
    "\3\0\2\5\1\0\1\5\3\0\3\5\4\0\11\5"+
    "\1\113\15\5\1\113\4\5\26\0\1\5\3\0\2\5"+
    "\1\0\1\114\3\0\3\5\4\0\15\5\1\115\7\5"+
    "\1\115\1\114\5\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\4\5\1\116\23\5\1\116"+
    "\3\5\26\0\1\5\3\0\2\5\1\0\1\5\3\0"+
    "\3\5\4\0\12\5\1\117\21\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\15\5\1\120"+
    "\7\5\1\120\6\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\1\5\1\100\16\5\1\101"+
    "\3\5\1\121\7\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\7\5\1\111\2\5\1\112"+
    "\15\5\1\122\3\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\7\5\1\104\15\5\1\123"+
    "\6\5\26\0\1\5\62\0\1\124\103\0\1\125\102\0"+
    "\1\126\1\127\103\0\1\130\23\0\6\131\1\0\1\131"+
    "\1\132\1\133\3\131\1\134\1\135\23\131\1\136\1\137"+
    "\1\140\1\131\1\141\3\131\1\142\25\131\1\143\1\144"+
    "\4\0\1\71\1\0\1\72\3\0\2\71\33\0\1\72"+
    "\40\0\1\145\2\0\1\146\2\0\2\145\41\0\1\146"+
    "\31\0\2\5\1\0\1\5\3\0\3\5\4\0\12\5"+
    "\1\147\21\5\26\0\1\5\3\0\2\5\1\0\1\5"+
    "\3\0\3\5\4\0\16\5\1\150\13\5\1\150\1\5"+
    "\26\0\1\5\2\0\6\76\1\151\75\76\14\77\1\152"+
    "\67\77\1\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\2\5\1\153\31\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\154\3\0\3\5\4\0\26\5\1\154\5\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\3\5\1\155\20\5\1\155\7\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\5\5\1\156"+
    "\25\5\1\156\26\0\1\5\3\0\2\5\1\0\1\5"+
    "\3\0\3\5\4\0\5\5\1\157\25\5\1\157\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\16\5\1\160\13\5\1\160\1\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\16\5\1\161"+
    "\13\5\1\161\1\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\12\5\1\162\21\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\2\5\1\163\31\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\3\5\1\164\20\5\1\164"+
    "\7\5\26\0\1\5\3\0\2\5\1\0\1\5\3\0"+
    "\3\5\4\0\13\5\1\165\20\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\5\5\1\166"+
    "\25\5\1\166\26\0\1\5\3\0\2\5\1\0\1\5"+
    "\3\0\3\5\4\0\4\5\1\167\23\5\1\167\3\5"+
    "\26\0\1\5\3\0\2\5\1\0\1\5\3\0\3\5"+
    "\4\0\16\5\1\170\13\5\1\170\1\5\26\0\1\5"+
    "\3\0\2\5\1\0\1\5\3\0\3\5\4\0\16\5"+
    "\1\171\13\5\1\171\1\5\26\0\1\5\3\0\2\5"+
    "\1\0\1\172\3\0\3\5\4\0\26\5\1\172\5\5"+
    "\26\0\1\5\3\0\2\5\1\0\1\5\3\0\3\5"+
    "\4\0\25\5\1\173\6\5\26\0\1\5\3\0\2\5"+
    "\1\0\1\5\3\0\3\5\4\0\31\5\1\174\2\5"+
    "\26\0\1\5\3\0\2\5\1\0\1\5\3\0\3\5"+
    "\4\0\31\5\1\175\2\5\26\0\1\5\12\0\2\176"+
    "\102\0\2\132\74\0\1\177\1\0\1\177\3\0\3\177"+
    "\10\0\1\177\1\0\1\177\2\0\1\177\5\0\1\177"+
    "\6\0\3\177\31\0\1\177\4\0\1\145\5\0\2\145"+
    "\73\0\2\5\1\0\1\5\3\0\3\5\4\0\16\5"+
    "\1\200\13\5\1\200\1\5\26\0\1\5\3\0\2\5"+
    "\1\0\1\201\3\0\3\5\4\0\26\5\1\201\5\5"+
    "\26\0\1\5\2\0\13\77\1\151\1\152\67\77\1\0"+
    "\2\5\1\0\1\202\3\0\3\5\4\0\26\5\1\202"+
    "\5\5\26\0\1\5\3\0\2\5\1\0\1\5\3\0"+
    "\3\5\4\0\5\5\1\203\25\5\1\203\26\0\1\5"+
    "\3\0\2\5\1\0\1\5\3\0\3\5\4\0\4\5"+
    "\1\204\23\5\1\204\3\5\26\0\1\5\3\0\2\5"+
    "\1\0\1\5\3\0\3\5\4\0\6\5\1\205\25\5"+
    "\26\0\1\5\3\0\2\5\1\0\1\5\3\0\3\5"+
    "\4\0\3\5\1\206\20\5\1\206\7\5\26\0\1\5"+
    "\3\0\2\5\1\0\1\5\3\0\3\5\4\0\1\207"+
    "\22\5\1\207\10\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\14\5\1\210\17\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\4\5\1\211\23\5\1\211\3\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\212\3\0\3\5\4\0\26\5\1\212"+
    "\5\5\26\0\1\5\3\0\2\5\1\0\1\213\3\0"+
    "\3\5\4\0\26\5\1\213\5\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\3\5\1\214"+
    "\20\5\1\214\7\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\215\3\0\3\5\4\0\26\5\1\215\5\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\1\216\22\5\1\216\10\5\26\0\1\5\3\0\2\5"+
    "\1\0\1\5\3\0\3\5\4\0\3\5\1\217\20\5"+
    "\1\217\7\5\26\0\1\5\3\0\2\5\1\0\1\5"+
    "\3\0\3\5\4\0\26\5\1\220\5\5\26\0\1\5"+
    "\3\0\2\5\1\0\1\5\3\0\3\5\4\0\32\5"+
    "\1\221\1\5\26\0\1\5\3\0\2\5\1\0\1\5"+
    "\3\0\3\5\4\0\31\5\1\222\2\5\26\0\1\5"+
    "\4\0\1\223\1\0\1\223\3\0\3\223\10\0\1\223"+
    "\1\0\1\223\2\0\1\223\5\0\1\223\6\0\3\223"+
    "\31\0\1\223\3\0\2\5\1\0\1\5\3\0\3\5"+
    "\4\0\1\224\22\5\1\224\10\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\10\5\1\225"+
    "\23\5\26\0\1\5\3\0\2\5\1\0\1\5\3\0"+
    "\3\5\4\0\7\5\1\226\24\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\4\5\1\227"+
    "\23\5\1\227\3\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\3\5\1\230\6\5\1\231"+
    "\11\5\1\230\7\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\12\5\1\232\21\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\21\5\1\233\7\5\1\233\2\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\6\5\1\234"+
    "\25\5\26\0\1\5\3\0\2\5\1\0\1\5\3\0"+
    "\3\5\4\0\3\5\1\235\20\5\1\235\7\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\4\5\1\236\23\5\1\236\3\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\1\5\1\237"+
    "\32\5\26\0\1\5\3\0\2\5\1\0\1\5\3\0"+
    "\3\5\4\0\26\5\1\240\5\5\26\0\1\5\4\0"+
    "\1\241\1\0\1\241\3\0\3\241\10\0\1\241\1\0"+
    "\1\241\2\0\1\241\5\0\1\241\6\0\3\241\31\0"+
    "\1\241\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\16\5\1\242\13\5\1\242\1\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\10\5\1\243"+
    "\23\5\26\0\1\5\3\0\2\5\1\0\1\5\3\0"+
    "\3\5\4\0\5\5\1\244\25\5\1\244\26\0\1\5"+
    "\3\0\2\5\1\0\1\5\3\0\3\5\4\0\12\5"+
    "\1\245\21\5\26\0\1\5\3\0\2\5\1\0\1\5"+
    "\3\0\3\5\4\0\5\5\1\246\25\5\1\246\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\5\5\1\247\25\5\1\247\26\0\1\5\3\0\2\5"+
    "\1\0\1\5\3\0\3\5\4\0\21\5\1\250\7\5"+
    "\1\250\2\5\26\0\1\5\3\0\2\5\1\0\1\5"+
    "\3\0\3\5\4\0\4\5\1\251\23\5\1\251\3\5"+
    "\26\0\1\5\3\0\2\5\1\0\1\5\3\0\3\5"+
    "\4\0\5\5\1\252\25\5\1\252\26\0\1\5\4\0"+
    "\1\253\1\0\1\253\3\0\3\253\10\0\1\253\1\0"+
    "\1\253\2\0\1\253\5\0\1\253\6\0\3\253\31\0"+
    "\1\253\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\6\5\1\254\25\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\2\5\1\255\4\0\34\5\26\0\1\255"+
    "\3\0\2\5\1\0\1\5\3\0\3\5\4\0\17\5"+
    "\1\256\14\5\26\0\1\5\3\0\2\5\1\0\1\5"+
    "\3\0\3\5\4\0\1\257\22\5\1\257\10\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\1\260\22\5\1\260\10\5\26\0\1\5\3\0\2\5"+
    "\1\0\1\5\3\0\3\5\4\0\7\5\1\261\24\5"+
    "\26\0\1\5\3\0\2\5\1\0\1\5\3\0\3\5"+
    "\4\0\15\5\1\262\7\5\1\262\6\5\26\0\1\5"+
    "\3\0\2\5\1\0\1\5\3\0\3\5\4\0\1\263"+
    "\22\5\1\263\10\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\12\5\1\264\21\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\265\3\0\3\5\4\0"+
    "\26\5\1\265\5\5\26\0\1\5\3\0\2\5\1\0"+
    "\1\5\3\0\3\5\4\0\10\5\1\234\23\5\26\0"+
    "\1\5\3\0\2\5\1\0\1\5\3\0\3\5\4\0"+
    "\1\266\22\5\1\266\10\5\26\0\1\5\3\0\2\5"+
    "\1\0\1\5\3\0\3\5\4\0\5\5\1\267\25\5"+
    "\1\267\26\0\1\5\3\0\2\5\1\0\1\270\3\0"+
    "\3\5\4\0\26\5\1\270\5\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\12\5\1\271"+
    "\21\5\26\0\1\5\3\0\2\5\1\0\1\5\3\0"+
    "\3\5\4\0\14\5\1\247\17\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\6\5\1\263"+
    "\25\5\26\0\1\5\3\0\2\5\1\0\1\5\3\0"+
    "\3\5\4\0\7\5\1\272\24\5\26\0\1\5\3\0"+
    "\2\5\1\0\1\5\3\0\3\5\4\0\5\5\1\273"+
    "\25\5\1\273\26\0\1\5\2\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[8364];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\3\0\1\11\5\1\1\11\1\1\3\11\20\1\3\11"+
    "\4\1\21\11\1\1\1\11\1\1\1\0\2\1\1\11"+
    "\1\1\1\0\24\1\6\11\2\1\4\11\1\1\4\11"+
    "\1\1\1\0\2\1\1\11\1\0\23\1\1\11\1\0"+
    "\23\1\1\0\15\1\1\0\11\1\1\11\20\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[187];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
  // For assembling string and character constants
  StringBuffer string_buf = new StringBuffer();

  // For line numbers
  public int getCurLineNum() {
    return yyline+1;
  }
  
  public int getCurColNum() {
    return yycolumn+1;
  }

  private String filename;

  public void setFilename(String fname) {
    filename = fname;
  }

  public String getCurFilename() {
    return filename;
  }
  
  private void error(int line, int col, String s) {
    errorMsg.error(line, col, s);
  }

  private void error(String s) {
    error(getCurLineNum(), getCurColNum(), s);
  }  
  
  private Symbol symbol(int type) {
    return symbol(type, null);
  }

  private Symbol symbol(int type, Object value) {
    return symbolFactory.newSymbol(yytext(), type, 
      new Location(getCurFilename(), getCurLineNum(), getCurColNum(), yychar+1), 
      new Location(getCurFilename(), getCurLineNum(), getCurColNum()+yylength(), yychar+1+yylength()), value);
  }
  
  blog.msg.ErrorMsg errorMsg; //for error
  private ComplexSymbolFactory symbolFactory; //for generating symbol

  public BLOGLexer(java.io.Reader r, ComplexSymbolFactory sf, blog.msg.ErrorMsg e){
    this(r);
    symbolFactory = sf;
    errorMsg=e;
  }



  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public BLOGLexer(java.io.Reader in) {
      // empty for now
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public BLOGLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader
             (in, java.nio.charset.Charset.forName("UTF-8")));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 194) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead > 0) {
      zzEndRead+= numRead;
      return false;
    }
    // unlikely but not impossible: read 0 characters, but not at end of stream    
    if (numRead == 0) {
      int c = zzReader.read();
      if (c == -1) {
        return true;
      } else {
        zzBuffer[zzEndRead++] = (char) c;
        return false;
      }     
    }

    // numRead < 0
    return true;
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * Internal scan buffer is resized down to its initial length, if it has grown.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEOFDone = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
    if (zzBuffer.length > ZZ_BUFFERSIZE)
      zzBuffer = new char[ZZ_BUFFERSIZE];
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() throws java.io.IOException {
    if (!zzEOFDone) {
      zzEOFDone = true;
      yyclose();
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public java_cup.runtime.Symbol next_token() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      yychar+= zzMarkedPosL-zzStartRead;

      boolean zzR = false;
      for (zzCurrentPosL = zzStartRead; zzCurrentPosL < zzMarkedPosL;
                                                             zzCurrentPosL++) {
        switch (zzBufferL[zzCurrentPosL]) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn++;
        }
      }

      if (zzR) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof) 
            zzPeek = false;
          else 
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 1: 
          { error("Lexer encountered some error");
          }
        case 76: break;
        case 2: 
          { return symbol(BLOGTokenConstants.ID, yytext());
          }
        case 77: break;
        case 3: 
          { return symbol(BLOGTokenConstants.INT_LITERAL, new Integer(yytext()));
          }
        case 78: break;
        case 4: 
          { return symbol(BLOGTokenConstants.DOT);
          }
        case 79: break;
        case 5: 
          { return symbol(BLOGTokenConstants.MINUS);
          }
        case 80: break;
        case 6: 
          { /* Do nothing */
          }
        case 81: break;
        case 7: 
          { return symbol(BLOGTokenConstants.DIV);
          }
        case 82: break;
        case 8: 
          { return symbol(BLOGTokenConstants.MULT);
          }
        case 83: break;
        case 9: 
          { yybegin (STR_LIT);
          }
        case 84: break;
        case 10: 
          { yybegin (CHAR_LIT);
          }
        case 85: break;
        case 11: 
          { return symbol(BLOGTokenConstants.PLUS);
          }
        case 86: break;
        case 12: 
          { return symbol(BLOGTokenConstants.MOD);
          }
        case 87: break;
        case 13: 
          { return symbol(BLOGTokenConstants.POWER);
          }
        case 88: break;
        case 14: 
          { return symbol(BLOGTokenConstants.LT);
          }
        case 89: break;
        case 15: 
          { return symbol(BLOGTokenConstants.GT);
          }
        case 90: break;
        case 16: 
          { return symbol(BLOGTokenConstants.EQ);
          }
        case 91: break;
        case 17: 
          { return symbol(BLOGTokenConstants.NOT);
          }
        case 92: break;
        case 18: 
          { return symbol(BLOGTokenConstants.AND);
          }
        case 93: break;
        case 19: 
          { return symbol(BLOGTokenConstants.OR);
          }
        case 94: break;
        case 20: 
          { return symbol(BLOGTokenConstants.AT);
          }
        case 95: break;
        case 21: 
          { return symbol(BLOGTokenConstants.LPAREN);
          }
        case 96: break;
        case 22: 
          { return symbol(BLOGTokenConstants.RPAREN);
          }
        case 97: break;
        case 23: 
          { return symbol(BLOGTokenConstants.RBRACE);
          }
        case 98: break;
        case 24: 
          { return symbol(BLOGTokenConstants.LBRACE);
          }
        case 99: break;
        case 25: 
          { return symbol(BLOGTokenConstants.LBRACKET);
          }
        case 100: break;
        case 26: 
          { return symbol(BLOGTokenConstants.RBRACKET);
          }
        case 101: break;
        case 27: 
          { return symbol(BLOGTokenConstants.SEMI);
          }
        case 102: break;
        case 28: 
          { return symbol(BLOGTokenConstants.COLON);
          }
        case 103: break;
        case 29: 
          { return symbol(BLOGTokenConstants.COMMA);
          }
        case 104: break;
        case 30: 
          { return symbol(BLOGTokenConstants.DISTRIB);
          }
        case 105: break;
        case 31: 
          { return symbol(BLOGTokenConstants.NUMSIGN);
          }
        case 106: break;
        case 32: 
          { /* Char in quotes, not matched by any rule above */
       string_buf.append(yytext());
          }
        case 107: break;
        case 33: 
          { error("Line terminator in string or character literal.");
          }
        case 108: break;
        case 34: 
          { /* closing double-quote not matched by \" rule below */
       Symbol s =   symbol(BLOGTokenConstants.STRING_LITERAL, 
			       string_buf.toString());
       string_buf = new StringBuffer(); /* reinitialize the buffer */
       yybegin(YYINITIAL);
       return s;
          }
        case 109: break;
        case 35: 
          { /* closing single-quote not matched by \' rule below */
       Symbol s;
       if (string_buf.length() == 1) {
	       s = symbol(BLOGTokenConstants.CHAR_LITERAL, 
                          new Character(string_buf.charAt(0)));
         string_buf.setLength(0); /* re-init buffer */
         yybegin(YYINITIAL);
         return s;
       } else {
  	     error("Character literal must contain exactly one character");
  	     yybegin(YYINITIAL);
       }
          }
        case 110: break;
        case 36: 
          { return 
		 symbol(BLOGTokenConstants.DOUBLE_LITERAL, new Double(yytext()));
          }
        case 111: break;
        case 37: 
          { return symbol(BLOGTokenConstants.RIGHTARROW);
          }
        case 112: break;
        case 38: 
          { /* ignore */
          }
        case 113: break;
        case 39: 
          { return symbol(BLOGTokenConstants.IF);
          }
        case 114: break;
        case 40: 
          { return symbol(BLOGTokenConstants.LEQ);
          }
        case 115: break;
        case 41: 
          { return symbol(BLOGTokenConstants.GEQ);
          }
        case 116: break;
        case 42: 
          { return symbol(BLOGTokenConstants.DOUBLERIGHTARROW);
          }
        case 117: break;
        case 43: 
          { return symbol(BLOGTokenConstants.EQEQ);
          }
        case 118: break;
        case 44: 
          { return symbol(BLOGTokenConstants.NEQ);
          }
        case 119: break;
        case 45: 
          { error("Unrecognized escape character: \'" 
                         + yytext() + "\'");
          }
        case 120: break;
        case 46: 
          { int code = Integer.parseInt(yytext().substring(1), 8);
         string_buf.append((char) code);
          }
        case 121: break;
        case 47: 
          { string_buf.append('\"');
          }
        case 122: break;
        case 48: 
          { string_buf.append('\'');
          }
        case 123: break;
        case 49: 
          { string_buf.append('\t');
          }
        case 124: break;
        case 50: 
          { string_buf.append('\r');
          }
        case 125: break;
        case 51: 
          { string_buf.append('\f');
          }
        case 126: break;
        case 52: 
          { string_buf.append('\n');
          }
        case 127: break;
        case 53: 
          { string_buf.append('\\');
          }
        case 128: break;
        case 54: 
          { string_buf.append('\b');
          }
        case 129: break;
        case 55: 
          { return symbol(BLOGTokenConstants.OBS);
          }
        case 130: break;
        case 56: 
          { return symbol(BLOGTokenConstants.MAP);
          }
        case 131: break;
        case 57: 
          { return symbol(BLOGTokenConstants.FOR);
          }
        case 132: break;
        case 58: 
          { return symbol(BLOGTokenConstants.ELSE);
          }
        case 133: break;
        case 59: 
          { return symbol(BLOGTokenConstants.TYPE);
          }
        case 134: break;
        case 60: 
          { return symbol(BLOGTokenConstants.THEN);
          }
        case 135: break;
        case 61: 
          { return symbol(BLOGTokenConstants.CASE);
          }
        case 136: break;
        case 62: 
          { return symbol(BLOGTokenConstants.LIST);
          }
        case 137: break;
        case 63: 
          { return symbol(BLOGTokenConstants.BOOLEAN_LITERAL, new Boolean(true));
          }
        case 138: break;
        case 64: 
          { return symbol(BLOGTokenConstants.NULL);
          }
        case 139: break;
        case 65: 
          { return symbol(BLOGTokenConstants.PARAM);
          }
        case 140: break;
        case 66: 
          { return symbol(BLOGTokenConstants.FIXED);
          }
        case 141: break;
        case 67: 
          { return symbol(BLOGTokenConstants.QUERY);
          }
        case 142: break;
        case 68: 
          { return symbol(BLOGTokenConstants.BOOLEAN_LITERAL, new Boolean(false));
          }
        case 143: break;
        case 69: 
          { return symbol(BLOGTokenConstants.EXISTS);
          }
        case 144: break;
        case 70: 
          { return symbol(BLOGTokenConstants.RANDOM);
          }
        case 145: break;
        case 71: 
          { return symbol(BLOGTokenConstants.ORIGIN);
          }
        case 146: break;
        case 72: 
          { return symbol(BLOGTokenConstants.FORALL);
          }
        case 147: break;
        case 73: 
          { int code = Integer.parseInt(yytext().substring(2), 16);
       string_buf.append((char) code);
          }
        case 148: break;
        case 74: 
          { return symbol(BLOGTokenConstants.DISTINCT);
          }
        case 149: break;
        case 75: 
          { return symbol(BLOGTokenConstants.DISTRIBUTION);
          }
        case 150: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            zzDoEOF();
              {   switch(yystate()) {
    case YYINITIAL:
    /* nothing special to do in the initial state */
      break;
    case STR_LIT:
    case CHAR_LIT:
      error("File ended before string or character literal "
                        + "was terminated.");
  }
  /* Reinitialize everything before signaling EOF */
  string_buf = new StringBuffer();
  yybegin(YYINITIAL);
  return symbol(BLOGTokenConstants.EOF);
 }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
