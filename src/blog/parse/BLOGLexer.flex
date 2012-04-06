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
 
/**
 * Using JFlex-1.4.3
 */ 
package blog.parse;
import java_cup.runtime.*;

%%

%class BLOGLexer
%implements ScannerWithLocInfo
%cup
%unicode
%line
%column
%public

%{
  // For assembling string and character constants
  StringBuffer string_buf = new StringBuffer();

  // For line numbers
  public int getCurLineNum() {
    return (yyline + 1);
  }

  private String filename;

  public void setFilename(String fname) {
    filename = fname;
  }

  public String getCurFilename() {
    return filename;
  }

    
%}



%init{
    // empty for now
%init}


%eofval{
  switch(yystate()) {
    case YYINITIAL:
    /* nothing special to do in the initial state */
      break;
    case PAREN_COMMENT:
      return new Symbol(BLOGTokenConstants.ERROR, 
                        "File ended before comment was terminated.");
    case STR_LIT:
    case CHAR_LIT:
      return new Symbol(BLOGTokenConstants.ERROR, 
                        "File ended before string or character literal "
                        + "was terminated.");

  }
  /* Reinitialize everything before signaling EOF */
  string_buf = new StringBuffer();
  yybegin(YYINITIAL);
  return new Symbol(BLOGTokenConstants.EOF);
%eofval}


Alpha = [A-Za-z]

Digit = [0-9]

Identifier = {Alpha}({Alpha}|{Digit}|_)*

IntegerConstant = {Digit}+

LineTerminator	= \n|\r|\r\n

Whitespace	= [ \f\t\n\r]

OCTAL_DIGIT     = [01234567]

ZERO_TO_THREE   = [0123]

HEX_DIGIT       = [0123456789abcdefABCDEF]

%state LINE_COMMENT, PAREN_COMMENT, STR_LIT, CHAR_LIT

%%

<YYINITIAL>{
{Whitespace} { /* Do nothing */}

"/*" { yybegin(PAREN_COMMENT); }
}

<PAREN_COMMENT>"*/" { yybegin(YYINITIAL); }
<PAREN_COMMENT>{LineTerminator} { /* do nothing */ }
<PAREN_COMMENT>. { /* do nothing */}


<YYINITIAL>"//" {yybegin(LINE_COMMENT); }
<LINE_COMMENT>{LineTerminator} { yybegin(YYINITIAL); } 
<LINE_COMMENT>. {}


<YYINITIAL>\" { yybegin (STR_LIT); }
<STR_LIT>\" { /* closing double-quote not matched by \" rule below */
       Symbol s =   new Symbol(BLOGTokenConstants.STR_CONST, 
			       string_buf.toString());
       string_buf = new StringBuffer(); /* reinitialize the buffer */
       yybegin(YYINITIAL);
       return s;}

<YYINITIAL>\' { yybegin (CHAR_LIT); }
<CHAR_LIT>\' { /* closing single-quote not matched by \' rule below */
       Symbol s;
       if (string_buf.length() == 1) {
	   s = new Symbol(BLOGTokenConstants.CHAR_CONST, 
                          new Character(string_buf.charAt(0)));
       } else {
	   s = new Symbol(BLOGTokenConstants.ERROR, 
                          "Character literal must contain exactly one "
                          + "character");
       } 
       string_buf = new StringBuffer(); /* re-init buffer */
       yybegin(YYINITIAL);
       return s; }

<STR_LIT,CHAR_LIT>\\b  { string_buf.append('\b'); }
<STR_LIT,CHAR_LIT>\\t  { string_buf.append('\t'); }
<STR_LIT,CHAR_LIT>\\n  { string_buf.append('\n'); }
<STR_LIT,CHAR_LIT>\\f  { string_buf.append('\f'); }
<STR_LIT,CHAR_LIT>\\r  { string_buf.append('\r'); }
<STR_LIT,CHAR_LIT>\\\" { string_buf.append('\"'); }
<STR_LIT,CHAR_LIT>\\\' { string_buf.append('\''); }
<STR_LIT,CHAR_LIT>\\\\ { string_buf.append('\\'); }

<STR_LIT,CHAR_LIT>\\{OCTAL_DIGIT} 
       { int code = Integer.parseInt(yytext().substring(1), 8);
         string_buf.append((char) code); }
<STR_LIT,CHAR_LIT>\\{OCTAL_DIGIT}{OCTAL_DIGIT} 
       { int code = Integer.parseInt(yytext().substring(1), 8);
         string_buf.append((char) code); }
<STR_LIT,CHAR_LIT>\\{ZERO_TO_THREE}{OCTAL_DIGIT}{OCTAL_DIGIT} 
       { int code = Integer.parseInt(yytext().substring(1), 8);
         string_buf.append((char) code); }

<STR_LIT,CHAR_LIT>\\u{HEX_DIGIT}{HEX_DIGIT}{HEX_DIGIT}{HEX_DIGIT} { 
       int code = Integer.parseInt(yytext().substring(2), 16);
       string_buf.append((char) code); }

<STR_LIT,CHAR_LIT>\\.  { 
       return new Symbol(BLOGTokenConstants.ERROR, 
                         "Unrecognized escape character: \'" 
                         + yytext() + "\'"); }

<STR_LIT,CHAR_LIT>{LineTerminator}  { 
       return new Symbol(BLOGTokenConstants.ERROR, 
                         "Line terminator in string or character literal."); }

<STR_LIT,CHAR_LIT>. { /* Char in quotes, not matched by any rule above */
       string_buf.append(yytext()); }


<YYINITIAL>-?[0-9]*[.][0-9]+ { return 
				    new Symbol(BLOGTokenConstants.DOUBLE_CONST,
					                           yytext()); }
<YYINITIAL>-?[0-9]+([.][0-9]+)?[Ee][+-]?[0-9]+ { return 
				    new Symbol(BLOGTokenConstants.DOUBLE_CONST,
					                           yytext()); }
<YYINITIAL>-?[.][0-9]+[Ee][+-]?[0-9]+ { return 
				    new Symbol(BLOGTokenConstants.DOUBLE_CONST,
					                           yytext()); }

<YYINITIAL>[0-9]+  { /* Integers */
                       return new Symbol(BLOGTokenConstants.INT_CONST,
					                           yytext()); }

<YYINITIAL>@[0-9]+ { return new Symbol(BLOGTokenConstants.TIME_CONST, 
				       yytext()); }

<YYINITIAL>[Tt][Yy][Pp][Ee]     { return new Symbol(BLOGTokenConstants.TYPE); }
<YYINITIAL>[Rr][Aa][Nn][Dd][Oo][Mm] { return 
				   new Symbol(BLOGTokenConstants.RANDOM); }
<YYINITIAL>[Nn][Oo][Nn][Rr][Aa][Nn][Dd][Oo][Mm] { return 
				   new Symbol(BLOGTokenConstants.NONRANDOM); }
<YYINITIAL>[Ff][Ii][Xx][Ee][Dd] { return 
				   new Symbol(BLOGTokenConstants.NONRANDOM); }			   
<YYINITIAL>[Gg][Ee][Nn][Ee][Rr][Aa][Tt][Ii][Nn][Gg] { return 
				   new Symbol(BLOGTokenConstants.GENERATING); }
<YYINITIAL>[Oo][Rr][Ii][Gg][Ii][Nn] { return 
                                   new Symbol(BLOGTokenConstants.GENERATING); }
<YYINITIAL>[Gg][Uu][Aa][Rr][Aa][Nn][Tt][Ee][Ee][Dd] { return 
				   new Symbol(BLOGTokenConstants.GUARANTEED); }
<YYINITIAL>[Dd][Ii][Ss][Tt][Ii][Nn][Cc][Tt] { return 
				   new Symbol(BLOGTokenConstants.GUARANTEED); }
<YYINITIAL>[Ff][Aa][Cc][Tt][Oo][Rr] { return 
                                   new Symbol(BLOGTokenConstants.FACTOR); }
<YYINITIAL>[Pp][Aa][Rr][Ff][Aa][Cc][Tt][Oo][Rr] { return
				   new Symbol(BLOGTokenConstants.PARFACTOR); }

<YYINITIAL>[Tt][Hh][Ee][Nn]     { return new Symbol(BLOGTokenConstants.THEN); }
<YYINITIAL>[Ee][Ll][Ss][Ee]  	{ return new Symbol(BLOGTokenConstants.ELSE); }
<YYINITIAL>[Ff][Oo][Rr]         { return new Symbol(BLOGTokenConstants.FOR); }
<YYINITIAL>[Ff][Aa][Ll][Ss][Ee]   { return new Symbol(BLOGTokenConstants.FALSE); }

<YYINITIAL>[Ee][Ll][Ss][Ee][Ii][Ff]  { return 
				       new Symbol(BLOGTokenConstants.ELSEIF); }
<YYINITIAL>[Ii][Ff]  		{ return new Symbol(BLOGTokenConstants.IF); }

<YYINITIAL>t[Rr][Uu][Ee]	{ return new Symbol(BLOGTokenConstants.TRUE); }
<YYINITIAL>[Qq][Uu][Ee][Rr][Yy]	{ return new Symbol(BLOGTokenConstants.QUERY);}
<YYINITIAL>[Oo][Bb][Ss]         { return new Symbol(BLOGTokenConstants.OBS);}
<YYINITIAL>"=="			{ return new Symbol(BLOGTokenConstants.EQ); }
<YYINITIAL>"="			{ return new Symbol(BLOGTokenConstants.ASSIGN); }
<YYINITIAL>"!="                 { return new Symbol(BLOGTokenConstants.NEQ); }
<YYINITIAL>"&"			{ return new Symbol(BLOGTokenConstants.AND); }
<YYINITIAL>"|"                  { return new Symbol(BLOGTokenConstants.OR); }
<YYINITIAL>"~"			{ return 
				    new Symbol(BLOGTokenConstants.DISTRIB); }
<YYINITIAL>"!"			{ return new Symbol(BLOGTokenConstants.NEG); }
<YYINITIAL>","			{ return new Symbol(BLOGTokenConstants.COMMA);}
<YYINITIAL>"->"			{ return 
				    new Symbol(BLOGTokenConstants.RIGHTARROW);}
<YYINITIAL>";"			{ return new Symbol(BLOGTokenConstants.SEMI); }
<YYINITIAL>":"			{ return new Symbol(BLOGTokenConstants.COLON);}
<YYINITIAL>"."                  { return new Symbol(BLOGTokenConstants.DOT); }
<YYINITIAL>"("			{ return 
				    new Symbol(BLOGTokenConstants.LPAREN); }
<YYINITIAL>")"			{ return 
				    new Symbol(BLOGTokenConstants.RPAREN); }
<YYINITIAL>"}"			{ return 
				    new Symbol(BLOGTokenConstants.RBRACE); }
<YYINITIAL>"{"			{ return 
				    new Symbol(BLOGTokenConstants.LBRACE); }
<YYINITIAL>"["			{ return 
				    new Symbol(BLOGTokenConstants.LBRACKET); }
<YYINITIAL>"]"			{ return 
				    new Symbol(BLOGTokenConstants.RBRACKET); }
<YYINITIAL>"#"                  { return 
                                    new Symbol(BLOGTokenConstants.NUMSIGN); } 
<YYINITIAL>"<"                  { return new Symbol(BLOGTokenConstants.LT); }
<YYINITIAL>">"                  { return new Symbol(BLOGTokenConstants.GT); }
<YYINITIAL>"<="                 { return new Symbol(BLOGTokenConstants.LEQ); }
<YYINITIAL>">="                 { return new Symbol(BLOGTokenConstants.GEQ); }
<YYINITIAL>"+"                 { return new Symbol(BLOGTokenConstants.PLUS); }
<YYINITIAL>"-"                 { return new Symbol(BLOGTokenConstants.MINUS); }

<YYINITIAL>[Ee][Xx][Ii][Ss][Tt][Ss] { /* existential quantifier */
	return new Symbol(BLOGTokenConstants.EXISTS); }
<YYINITIAL>[Ff][Oo][Rr][Aa][Ll][Ll] { /* universal quantifier */
	return new Symbol(BLOGTokenConstants.FORALL); }
<YYINITIAL>[A-Za-z][A-Za-z0-9_]*  {
        return new Symbol(BLOGTokenConstants.ID, yytext()); }
<YYINITIAL>[A-Za-z][A-Za-z0-9_]*([.][A-Za-z][A-Za-z0-9_]*)* {
        return new Symbol(BLOGTokenConstants.CLASS_NAME, yytext()); }
<YYINITIAL>.                     { return new Symbol(BLOGTokenConstants.ERROR, 
                                          yytext()); }


.             { /*
                    *  This should be the very last rule and will match
                    *  everything not matched by other lexical rules.
                    */
                   System.err.println("LEXER BUG - UNMATCHED: " + yytext()); }
