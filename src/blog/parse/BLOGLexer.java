/* The following code was generated by JFlex 1.4.3 on 6/15/14 9:53 PM */

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
 * Using JFlex-1.4.3
 * @author leili
 */ 
package blog.parse;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.3
 * on 6/15/14 9:53 PM from the specification file
 * <tt>src/blog/parse/BLOGLexer.flex</tt>
 */
public class BLOGLexer implements ScannerWithLocInfo, java_cup.runtime.Scanner {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int CHAR_LIT = 4;
  public static final int YYINITIAL = 0;
  public static final int STR_LIT = 2;

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
    "\11\0\1\7\1\102\1\0\1\7\1\6\22\0\1\7\1\61\1\15"+
    "\1\77\1\0\1\54\1\62\1\16\1\65\1\66\1\14\1\53\1\75"+
    "\1\3\1\4\1\13\4\11\4\10\2\2\1\74\1\73\1\56\1\60"+
    "\1\57\1\0\1\64\1\23\1\12\1\36\1\25\1\5\1\30\1\33"+
    "\1\37\1\31\2\1\1\40\1\27\1\24\1\26\1\21\1\41\1\22"+
    "\1\35\1\17\1\34\2\1\1\32\1\20\1\1\1\71\1\100\1\72"+
    "\1\55\1\1\1\0\1\47\1\101\1\36\1\25\1\45\1\46\1\33"+
    "\1\37\1\31\2\1\1\50\1\27\1\52\1\26\1\21\1\41\1\43"+
    "\1\51\1\42\1\44\2\1\1\32\1\20\1\1\1\70\1\63\1\67"+
    "\1\76\uff81\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\3\0\1\1\1\2\1\3\1\4\1\5\1\2\1\6"+
    "\1\7\1\10\1\11\1\12\17\2\1\13\1\14\1\15"+
    "\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25"+
    "\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35"+
    "\1\36\1\37\1\40\1\41\1\42\1\40\1\43\1\44"+
    "\2\0\1\45\2\2\1\46\1\0\13\2\1\47\7\2"+
    "\1\50\1\51\1\52\1\53\1\54\1\55\2\56\1\57"+
    "\1\60\1\61\1\62\1\55\1\63\1\64\1\65\1\66"+
    "\1\44\1\0\2\2\1\46\1\0\6\2\1\67\1\2"+
    "\1\70\1\71\10\2\1\56\1\0\1\2\1\72\1\73"+
    "\1\74\11\2\1\75\1\2\1\76\1\2\1\77\1\0"+
    "\1\2\1\100\6\2\1\101\2\2\1\102\1\103\1\0"+
    "\1\104\1\105\3\2\1\106\1\107\2\2\1\110\7\2"+
    "\1\111\7\2\1\112";

  private static int [] zzUnpackAction() {
    int [] result = new int[184];
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
    "\0\0\0\103\0\206\0\311\0\u010c\0\u014f\0\u0192\0\u01d5"+
    "\0\u0218\0\311\0\u025b\0\311\0\311\0\311\0\u029e\0\u02e1"+
    "\0\u0324\0\u0367\0\u03aa\0\u03ed\0\u0430\0\u0473\0\u04b6\0\u04f9"+
    "\0\u053c\0\u057f\0\u05c2\0\u0605\0\u0648\0\u068b\0\311\0\311"+
    "\0\u06ce\0\u0711\0\u0754\0\u0797\0\311\0\311\0\311\0\311"+
    "\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311"+
    "\0\311\0\311\0\311\0\311\0\311\0\u07da\0\311\0\u081d"+
    "\0\u0860\0\u01d5\0\311\0\u08a3\0\u08e6\0\u0929\0\u096c\0\u09af"+
    "\0\u09f2\0\u0a35\0\u0a78\0\u0abb\0\u0afe\0\u0b41\0\u0b84\0\u0bc7"+
    "\0\u0c0a\0\u0c4d\0\u010c\0\u0c90\0\u0cd3\0\u0d16\0\u0d59\0\u0d9c"+
    "\0\u0ddf\0\u0e22\0\311\0\311\0\311\0\311\0\311\0\311"+
    "\0\u0e65\0\u0ea8\0\311\0\311\0\311\0\311\0\u0eeb\0\311"+
    "\0\311\0\311\0\311\0\u0f2e\0\u0f2e\0\u0f71\0\u0fb4\0\311"+
    "\0\u0ff7\0\u103a\0\u107d\0\u10c0\0\u1103\0\u1146\0\u1189\0\u010c"+
    "\0\u11cc\0\u010c\0\u120f\0\u1252\0\u1295\0\u12d8\0\u131b\0\u135e"+
    "\0\u13a1\0\u13e4\0\u1427\0\311\0\u146a\0\u14ad\0\u010c\0\u010c"+
    "\0\u010c\0\u14f0\0\u1533\0\u1576\0\u15b9\0\u15fc\0\u163f\0\u1682"+
    "\0\u16c5\0\u1708\0\u010c\0\u174b\0\u010c\0\u178e\0\u010c\0\u17d1"+
    "\0\u1814\0\u010c\0\u1857\0\u189a\0\u18dd\0\u1920\0\u1963\0\u19a6"+
    "\0\u010c\0\u19e9\0\u1a2c\0\u010c\0\u010c\0\u1a6f\0\u010c\0\u010c"+
    "\0\u1ab2\0\u1af5\0\u1b38\0\u010c\0\u010c\0\u1b7b\0\u1bbe\0\311"+
    "\0\u1c01\0\u1c44\0\u1c87\0\u1cca\0\u1d0d\0\u1d50\0\u1d93\0\u010c"+
    "\0\u1dd6\0\u1e19\0\u1e5c\0\u1e9f\0\u1ee2\0\u1f25\0\u1f68\0\u010c";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[184];
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
    "\1\5\1\30\4\5\1\31\1\32\1\33\1\21\1\5"+
    "\1\11\1\34\1\5\1\31\1\5\1\35\1\36\1\37"+
    "\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47"+
    "\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57"+
    "\1\60\1\61\1\62\1\4\1\5\1\12\6\63\1\64"+
    "\6\63\1\65\62\63\1\66\1\63\1\64\6\63\1\64"+
    "\7\63\1\67\61\63\1\66\1\63\1\64\104\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\34\5\26\0\1\5"+
    "\3\0\1\6\1\0\1\70\1\71\2\0\2\6\33\0"+
    "\1\71\37\0\1\6\1\0\1\72\3\0\2\6\45\0"+
    "\1\73\25\0\1\70\5\0\2\70\72\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\13\5\1\74\5\5\1\75"+
    "\7\5\1\75\2\5\26\0\1\5\14\0\1\76\1\77"+
    "\67\0\2\5\2\0\1\5\2\0\3\5\4\0\1\5"+
    "\1\100\16\5\1\101\13\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\4\5\1\102\23\5"+
    "\1\102\3\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\4\5\1\103\23\5\1\103\3\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\7\5\1\104\24\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\12\5\1\105\21\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\2\5"+
    "\1\106\4\0\3\5\1\107\20\5\1\107\7\5\26\0"+
    "\1\106\2\0\2\5\2\0\1\5\2\0\3\5\4\0"+
    "\4\5\1\110\23\5\1\110\3\5\26\0\1\5\2\0"+
    "\2\5\2\0\1\5\2\0\3\5\4\0\7\5\1\111"+
    "\2\5\1\112\21\5\26\0\1\5\2\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\11\5\1\113\15\5\1\113"+
    "\4\5\26\0\1\5\2\0\2\5\2\0\1\114\2\0"+
    "\3\5\4\0\15\5\1\115\7\5\1\115\1\114\5\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\12\5\1\116\21\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\15\5\1\117\7\5"+
    "\1\117\6\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\1\5\1\100\16\5\1\101\3\5"+
    "\1\120\7\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\7\5\1\111\2\5\1\112\15\5"+
    "\1\121\3\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\7\5\1\104\15\5\1\122\6\5"+
    "\26\0\1\5\3\0\1\6\1\0\1\72\3\0\2\6"+
    "\151\0\1\123\102\0\1\124\101\0\1\125\1\126\102\0"+
    "\1\127\22\0\10\130\1\131\1\132\3\130\1\133\1\134"+
    "\23\130\1\135\1\136\1\137\1\130\1\140\3\130\1\141"+
    "\25\130\1\142\1\143\3\0\1\70\2\0\1\71\2\0"+
    "\2\70\33\0\1\71\37\0\1\144\1\145\4\0\2\144"+
    "\41\0\1\145\30\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\12\5\1\146\21\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\16\5\1\147\13\5"+
    "\1\147\1\5\26\0\1\5\1\0\6\76\1\150\73\76"+
    "\1\150\14\77\1\151\66\77\1\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\2\5\1\152\31\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\153\2\0\3\5\4\0\26\5"+
    "\1\153\5\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\3\5\1\154\20\5\1\154\7\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\5\5\1\155\25\5\1\155\26\0\1\5\2\0"+
    "\2\5\2\0\1\5\2\0\3\5\4\0\5\5\1\156"+
    "\25\5\1\156\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\16\5\1\157\13\5\1\157\1\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\16\5\1\160\13\5\1\160\1\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\12\5"+
    "\1\161\21\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\2\5\1\162\31\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\3\5"+
    "\1\163\20\5\1\163\7\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\13\5\1\164\20\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\5\5\1\165\25\5\1\165\26\0\1\5\2\0"+
    "\2\5\2\0\1\5\2\0\3\5\4\0\4\5\1\166"+
    "\23\5\1\166\3\5\26\0\1\5\2\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\16\5\1\167\13\5\1\167"+
    "\1\5\26\0\1\5\2\0\2\5\2\0\1\170\2\0"+
    "\3\5\4\0\26\5\1\170\5\5\26\0\1\5\2\0"+
    "\2\5\2\0\1\5\2\0\3\5\4\0\25\5\1\171"+
    "\6\5\26\0\1\5\2\0\2\5\2\0\1\5\2\0"+
    "\3\5\4\0\31\5\1\172\2\5\26\0\1\5\2\0"+
    "\2\5\2\0\1\5\2\0\3\5\4\0\31\5\1\173"+
    "\2\5\26\0\1\5\11\0\2\174\101\0\2\131\73\0"+
    "\1\175\2\0\1\175\2\0\3\175\10\0\1\175\1\0"+
    "\1\175\2\0\1\175\5\0\1\175\6\0\3\175\31\0"+
    "\1\175\3\0\1\144\5\0\2\144\72\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\16\5\1\176\13\5\1\176"+
    "\1\5\26\0\1\5\2\0\2\5\2\0\1\177\2\0"+
    "\3\5\4\0\26\5\1\177\5\5\26\0\1\5\1\0"+
    "\13\77\1\150\1\151\66\77\1\0\2\5\2\0\1\200"+
    "\2\0\3\5\4\0\26\5\1\200\5\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\5\5"+
    "\1\201\25\5\1\201\26\0\1\5\2\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\4\5\1\202\23\5\1\202"+
    "\3\5\26\0\1\5\2\0\2\5\2\0\1\5\2\0"+
    "\3\5\4\0\6\5\1\203\25\5\26\0\1\5\2\0"+
    "\2\5\2\0\1\5\2\0\3\5\4\0\3\5\1\204"+
    "\20\5\1\204\7\5\26\0\1\5\2\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\1\205\22\5\1\205\10\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\14\5\1\206\17\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\4\5\1\207\23\5"+
    "\1\207\3\5\26\0\1\5\2\0\2\5\2\0\1\210"+
    "\2\0\3\5\4\0\26\5\1\210\5\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\211\2\0\3\5\4\0\26\5"+
    "\1\211\5\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\3\5\1\212\20\5\1\212\7\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\1\213\22\5\1\213\10\5\26\0\1\5\2\0"+
    "\2\5\2\0\1\5\2\0\3\5\4\0\3\5\1\214"+
    "\20\5\1\214\7\5\26\0\1\5\2\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\26\5\1\215\5\5\26\0"+
    "\1\5\2\0\2\5\2\0\1\5\2\0\3\5\4\0"+
    "\32\5\1\216\1\5\26\0\1\5\2\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\31\5\1\217\2\5\26\0"+
    "\1\5\3\0\1\220\2\0\1\220\2\0\3\220\10\0"+
    "\1\220\1\0\1\220\2\0\1\220\5\0\1\220\6\0"+
    "\3\220\31\0\1\220\2\0\2\5\2\0\1\5\2\0"+
    "\3\5\4\0\1\221\22\5\1\221\10\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\10\5"+
    "\1\222\23\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\7\5\1\223\24\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\4\5"+
    "\1\224\23\5\1\224\3\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\3\5\1\225\6\5"+
    "\1\226\11\5\1\225\7\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\12\5\1\227\21\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\21\5\1\230\7\5\1\230\2\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\6\5"+
    "\1\231\25\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\3\5\1\232\20\5\1\232\7\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\4\5\1\233\23\5\1\233\3\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\1\5"+
    "\1\234\32\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\26\5\1\235\5\5\26\0\1\5"+
    "\3\0\1\236\2\0\1\236\2\0\3\236\10\0\1\236"+
    "\1\0\1\236\2\0\1\236\5\0\1\236\6\0\3\236"+
    "\31\0\1\236\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\16\5\1\237\13\5\1\237\1\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\10\5"+
    "\1\240\23\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\5\5\1\241\25\5\1\241\26\0"+
    "\1\5\2\0\2\5\2\0\1\5\2\0\3\5\4\0"+
    "\12\5\1\242\21\5\26\0\1\5\2\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\5\5\1\243\25\5\1\243"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\5\5\1\244\25\5\1\244\26\0\1\5\2\0"+
    "\2\5\2\0\1\5\2\0\3\5\4\0\21\5\1\245"+
    "\7\5\1\245\2\5\26\0\1\5\2\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\4\5\1\246\23\5\1\246"+
    "\3\5\26\0\1\5\2\0\2\5\2\0\1\5\2\0"+
    "\3\5\4\0\5\5\1\247\25\5\1\247\26\0\1\5"+
    "\3\0\1\250\2\0\1\250\2\0\3\250\10\0\1\250"+
    "\1\0\1\250\2\0\1\250\5\0\1\250\6\0\3\250"+
    "\31\0\1\250\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\6\5\1\251\25\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\2\5\1\252\4\0\34\5\26\0"+
    "\1\252\2\0\2\5\2\0\1\5\2\0\3\5\4\0"+
    "\17\5\1\253\14\5\26\0\1\5\2\0\2\5\2\0"+
    "\1\5\2\0\3\5\4\0\1\254\22\5\1\254\10\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\1\255\22\5\1\255\10\5\26\0\1\5\2\0"+
    "\2\5\2\0\1\5\2\0\3\5\4\0\7\5\1\256"+
    "\24\5\26\0\1\5\2\0\2\5\2\0\1\5\2\0"+
    "\3\5\4\0\15\5\1\257\7\5\1\257\6\5\26\0"+
    "\1\5\2\0\2\5\2\0\1\5\2\0\3\5\4\0"+
    "\1\260\22\5\1\260\10\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\12\5\1\261\21\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\262\2\0\3\5"+
    "\4\0\26\5\1\262\5\5\26\0\1\5\2\0\2\5"+
    "\2\0\1\5\2\0\3\5\4\0\10\5\1\231\23\5"+
    "\26\0\1\5\2\0\2\5\2\0\1\5\2\0\3\5"+
    "\4\0\1\263\22\5\1\263\10\5\26\0\1\5\2\0"+
    "\2\5\2\0\1\5\2\0\3\5\4\0\5\5\1\264"+
    "\25\5\1\264\26\0\1\5\2\0\2\5\2\0\1\265"+
    "\2\0\3\5\4\0\26\5\1\265\5\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\12\5"+
    "\1\266\21\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\14\5\1\244\17\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\6\5"+
    "\1\260\25\5\26\0\1\5\2\0\2\5\2\0\1\5"+
    "\2\0\3\5\4\0\7\5\1\267\24\5\26\0\1\5"+
    "\2\0\2\5\2\0\1\5\2\0\3\5\4\0\5\5"+
    "\1\270\25\5\1\270\26\0\1\5\1\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[8107];
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
    "\3\0\1\11\5\1\1\11\1\1\3\11\20\1\2\11"+
    "\4\1\21\11\1\1\1\11\1\1\2\0\1\11\3\1"+
    "\1\0\23\1\6\11\2\1\4\11\1\1\4\11\1\1"+
    "\1\0\2\1\1\11\1\0\22\1\1\11\1\0\22\1"+
    "\1\0\15\1\1\0\11\1\1\11\20\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[184];
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
  
  private void err(int line, int col, String s) {
    errorMsg.error(line, col, s);
  }

  private void err(String s) {
    err(getCurLineNum(), getCurColNum(), s);
  }  
  
  private Symbol symbol(int type) {
    return symbol(type, null);
  }

  private Symbol symbol(int type, Object value) {
    //return new BLOGSymbol(type, getCurLineNum(), getCurColNum(), yychar, yychar+yylength(), value);
    return symbolFactory.newSymbol("token", type, new Location(yyline+1, yycolumn +1), new Location(yyline+1,yycolumn+yylength()), value);
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
    this(new java.io.InputStreamReader(in));
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
    while (i < 184) {
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

          int zzAttributes = zzAttrL[zzState];
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
        case 67: 
          { return symbol(BLOGTokenConstants.BOOLEAN_LITERAL, new Boolean(false));
          }
        case 75: break;
        case 64: 
          { return symbol(BLOGTokenConstants.PARAM);
          }
        case 76: break;
        case 7: 
          { return symbol(BLOGTokenConstants.DIV);
          }
        case 77: break;
        case 2: 
          { return symbol(BLOGTokenConstants.ID, yytext());
          }
        case 78: break;
        case 52: 
          { string_buf.append('\n');
          }
        case 79: break;
        case 47: 
          { string_buf.append('\"');
          }
        case 80: break;
        case 12: 
          { return symbol(BLOGTokenConstants.MOD);
          }
        case 81: break;
        case 33: 
          { return symbol(BLOGTokenConstants.ERROR, 
                         "Line terminator in string or character literal.");
          }
        case 82: break;
        case 8: 
          { return symbol(BLOGTokenConstants.MULT);
          }
        case 83: break;
        case 39: 
          { return symbol(BLOGTokenConstants.IF);
          }
        case 84: break;
        case 29: 
          { return symbol(BLOGTokenConstants.COMMA);
          }
        case 85: break;
        case 36: 
          { return 
		 symbol(BLOGTokenConstants.DOUBLE_LITERAL, new Double(yytext()));
          }
        case 86: break;
        case 22: 
          { return symbol(BLOGTokenConstants.RPAREN);
          }
        case 87: break;
        case 50: 
          { string_buf.append('\r');
          }
        case 88: break;
        case 58: 
          { return symbol(BLOGTokenConstants.ELSE);
          }
        case 89: break;
        case 48: 
          { string_buf.append('\'');
          }
        case 90: break;
        case 44: 
          { return symbol(BLOGTokenConstants.NEQ);
          }
        case 91: break;
        case 49: 
          { string_buf.append('\t');
          }
        case 92: break;
        case 13: 
          { return symbol(BLOGTokenConstants.POWER);
          }
        case 93: break;
        case 21: 
          { return symbol(BLOGTokenConstants.LPAREN);
          }
        case 94: break;
        case 5: 
          { return symbol(BLOGTokenConstants.DOT);
          }
        case 95: break;
        case 73: 
          { return symbol(BLOGTokenConstants.DISTINCT);
          }
        case 96: break;
        case 72: 
          { int code = Integer.parseInt(yytext().substring(2), 16);
       string_buf.append((char) code);
          }
        case 97: break;
        case 18: 
          { return symbol(BLOGTokenConstants.AND);
          }
        case 98: break;
        case 65: 
          { return symbol(BLOGTokenConstants.FIXED);
          }
        case 99: break;
        case 56: 
          { return symbol(BLOGTokenConstants.MAP);
          }
        case 100: break;
        case 42: 
          { return symbol(BLOGTokenConstants.DOUBLERIGHTARROW);
          }
        case 101: break;
        case 6: 
          { /* Do nothing */
          }
        case 102: break;
        case 3: 
          { return symbol(BLOGTokenConstants.INT_LITERAL, new Integer(yytext()));
          }
        case 103: break;
        case 17: 
          { return symbol(BLOGTokenConstants.NOT);
          }
        case 104: break;
        case 61: 
          { return symbol(BLOGTokenConstants.LIST);
          }
        case 105: break;
        case 27: 
          { return symbol(BLOGTokenConstants.SEMI);
          }
        case 106: break;
        case 66: 
          { return symbol(BLOGTokenConstants.QUERY);
          }
        case 107: break;
        case 20: 
          { return symbol(BLOGTokenConstants.AT);
          }
        case 108: break;
        case 1: 
          { return symbol(BLOGTokenConstants.ERROR, 
                                          yytext());
          }
        case 109: break;
        case 9: 
          { yybegin (STR_LIT);
          }
        case 110: break;
        case 43: 
          { return symbol(BLOGTokenConstants.EQEQ);
          }
        case 111: break;
        case 63: 
          { return symbol(BLOGTokenConstants.NULL);
          }
        case 112: break;
        case 11: 
          { return symbol(BLOGTokenConstants.PLUS);
          }
        case 113: break;
        case 41: 
          { return symbol(BLOGTokenConstants.GEQ);
          }
        case 114: break;
        case 30: 
          { return symbol(BLOGTokenConstants.DISTRIB);
          }
        case 115: break;
        case 62: 
          { return symbol(BLOGTokenConstants.BOOLEAN_LITERAL, new Boolean(true));
          }
        case 116: break;
        case 74: 
          { return symbol(BLOGTokenConstants.DISTRIBUTION);
          }
        case 117: break;
        case 53: 
          { string_buf.append('\\');
          }
        case 118: break;
        case 40: 
          { return symbol(BLOGTokenConstants.LEQ);
          }
        case 119: break;
        case 35: 
          { /* closing single-quote not matched by \' rule below */
       Symbol s;
       if (string_buf.length() == 1) {
	   s = symbol(BLOGTokenConstants.CHAR_LITERAL, 
                          new Character(string_buf.charAt(0)));
       } else {
	   s = symbol(BLOGTokenConstants.ERROR, 
                          "Character literal must contain exactly one "
                          + "character");
       } 
       string_buf = new StringBuffer(); /* re-init buffer */
       yybegin(YYINITIAL);
       return s;
          }
        case 120: break;
        case 15: 
          { return symbol(BLOGTokenConstants.GT);
          }
        case 121: break;
        case 60: 
          { return symbol(BLOGTokenConstants.THEN);
          }
        case 122: break;
        case 16: 
          { return symbol(BLOGTokenConstants.EQ);
          }
        case 123: break;
        case 23: 
          { return symbol(BLOGTokenConstants.RBRACE);
          }
        case 124: break;
        case 46: 
          { int code = Integer.parseInt(yytext().substring(1), 8);
         string_buf.append((char) code);
          }
        case 125: break;
        case 54: 
          { string_buf.append('\b');
          }
        case 126: break;
        case 14: 
          { return symbol(BLOGTokenConstants.LT);
          }
        case 127: break;
        case 70: 
          { return symbol(BLOGTokenConstants.ORIGIN);
          }
        case 128: break;
        case 10: 
          { yybegin (CHAR_LIT);
          }
        case 129: break;
        case 69: 
          { return symbol(BLOGTokenConstants.RANDOM);
          }
        case 130: break;
        case 34: 
          { /* closing double-quote not matched by \" rule below */
       Symbol s =   symbol(BLOGTokenConstants.STRING_LITERAL, 
			       string_buf.toString());
       string_buf = new StringBuffer(); /* reinitialize the buffer */
       yybegin(YYINITIAL);
       return s;
          }
        case 131: break;
        case 32: 
          { /* Char in quotes, not matched by any rule above */
       string_buf.append(yytext());
          }
        case 132: break;
        case 71: 
          { return symbol(BLOGTokenConstants.FORALL);
          }
        case 133: break;
        case 51: 
          { string_buf.append('\f');
          }
        case 134: break;
        case 38: 
          { /* ignore */
          }
        case 135: break;
        case 55: 
          { return symbol(BLOGTokenConstants.OBS);
          }
        case 136: break;
        case 24: 
          { return symbol(BLOGTokenConstants.LBRACE);
          }
        case 137: break;
        case 25: 
          { return symbol(BLOGTokenConstants.LBRACKET);
          }
        case 138: break;
        case 19: 
          { return symbol(BLOGTokenConstants.OR);
          }
        case 139: break;
        case 4: 
          { return symbol(BLOGTokenConstants.MINUS);
          }
        case 140: break;
        case 68: 
          { return symbol(BLOGTokenConstants.EXISTS);
          }
        case 141: break;
        case 57: 
          { return symbol(BLOGTokenConstants.FOR);
          }
        case 142: break;
        case 45: 
          { return symbol(BLOGTokenConstants.ERROR, 
                         "Unrecognized escape character: \'" 
                         + yytext() + "\'");
          }
        case 143: break;
        case 28: 
          { return symbol(BLOGTokenConstants.COLON);
          }
        case 144: break;
        case 26: 
          { return symbol(BLOGTokenConstants.RBRACKET);
          }
        case 145: break;
        case 31: 
          { return symbol(BLOGTokenConstants.NUMSIGN);
          }
        case 146: break;
        case 59: 
          { return symbol(BLOGTokenConstants.TYPE);
          }
        case 147: break;
        case 37: 
          { return symbol(BLOGTokenConstants.RIGHTARROW);
          }
        case 148: break;
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
      return symbol(BLOGTokenConstants.ERROR, 
                        "File ended before string or character literal "
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
