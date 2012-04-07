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
  
  private Symbol symbol(int type) {
    return new BLOGSymbol(type, yyline+1, yycolumn+1);
  }

  private Symbol symbol(int type, Object value) {
    return new BLOGSymbol(type, yyline+1, yycolumn+1, value);
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
%eofval}


Alpha = [A-Za-z]

Digit = [0-9]

Identifier = {Alpha}({Alpha}|{Digit}|_)*

IntegerLiteral = {Digit}+

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

TimeLiteral = @{Digit}+

LineTerminator	= \n|\r|\r\n

InputCharacter = [^\r\n]

Whitespace	= [ \f\t\n\r]

OCTAL_DIGIT     = [01234567]

ZERO_TO_THREE   = [0123]

HEX_DIGIT       = [0123456789abcdefABCDEF]

/* comments */
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/*" "*"+ [^/*] ~"*/"
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}

%state STR_LIT, CHAR_LIT

%%

<YYINITIAL>{
\" { yybegin (STR_LIT); }
\' { yybegin (CHAR_LIT); }

/* keywords */
[Tt][Yy][Pp][Ee] { return symbol(BLOGTokenConstants.TYPE); }
[Rr][Aa][Nn][Dd][Oo][Mm] { return symbol(BLOGTokenConstants.RANDOM); }
[Nn][Oo][Nn][Rr][Aa][Nn][Dd][Oo][Mm] { return symbol(BLOGTokenConstants.NONRANDOM); }
[Ff][Ii][Xx][Ee][Dd] { return symbol(BLOGTokenConstants.NONRANDOM); }			   
[Gg][Ee][Nn][Ee][Rr][Aa][Tt][Ii][Nn][Gg] { return symbol(BLOGTokenConstants.GENERATING); }
[Oo][Rr][Ii][Gg][Ii][Nn] { return symbol(BLOGTokenConstants.GENERATING); }
[Gg][Uu][Aa][Rr][Aa][Nn][Tt][Ee][Ee][Dd] { return symbol(BLOGTokenConstants.GUARANTEED); }
[Dd][Ii][Ss][Tt][Ii][Nn][Cc][Tt] { return symbol(BLOGTokenConstants.GUARANTEED); }
[Ff][Aa][Cc][Tt][Oo][Rr] { return symbol(BLOGTokenConstants.FACTOR); }
[Pp][Aa][Rr][Ff][Aa][Cc][Tt][Oo][Rr] { return symbol(BLOGTokenConstants.PARFACTOR); }
[Tt][Hh][Ee][Nn]     { return symbol(BLOGTokenConstants.THEN); }
[Ee][Ll][Ss][Ee]  	{ return symbol(BLOGTokenConstants.ELSE); }
[Ff][Oo][Rr]         { return symbol(BLOGTokenConstants.FOR); }
[Ee][Ll][Ss][Ee][Ii][Ff]  { return symbol(BLOGTokenConstants.ELSEIF); }
[Ii][Ff]  		{ return symbol(BLOGTokenConstants.IF); }
[Qq][Uu][Ee][Rr][Yy]	{ return symbol(BLOGTokenConstants.QUERY);}
[Oo][Bb][Ss]         { return symbol(BLOGTokenConstants.OBS);}
[Pp][Aa][Rr][Aa][Mm] { return symbol(BLOGTokenConstants.PARAM);}
[Ee][Xx][Ii][Ss][Tt][Ss] { /* existential quantifier */
	return symbol(BLOGTokenConstants.EXISTS); }
[Ff][Oo][Rr][Aa][Ll][Ll] { /* universal quantifier */
	return symbol(BLOGTokenConstants.FORALL); }
	

/* literals */
"true"	{ return symbol(BLOGTokenConstants.TRUE, new Boolean(true)); }
"false"   { return symbol(BLOGTokenConstants.FALSE, new Boolean(false)); }
"null" {return symbol(BLOGTokenConstants.NULL); }
{DoubleLiteral} { return 
		 symbol(BLOGTokenConstants.DOUBLE_LITERAL, new Double(yytext())); }
{IntegerLiteral}  { /* Integers */
     return symbol(BLOGTokenConstants.INT_LITERAL, new Integer(yytext())); }
{TimeLiteral} { return symbol(BLOGTokenConstants.TIME_LITERAL, new Integer(yytext())); }

/* operators */
"+"     { return symbol(BLOGTokenConstants.PLUS); }
"-"     { return symbol(BLOGTokenConstants.MINUS); }
"*"     { return symbol(BLOGTokenConstants.MULT); }
"/"     { return symbol(BLOGTokenConstants.DIV); }
"%"     { return symbol(BLOGTokenConstants.MOD); }
"<"     { return symbol(BLOGTokenConstants.LT); }
">"     { return symbol(BLOGTokenConstants.GT); }
"<="    { return symbol(BLOGTokenConstants.LEQ); }
">="    { return symbol(BLOGTokenConstants.GEQ); }
"=="		{ return symbol(BLOGTokenConstants.EQEQ); }
"!="    { return symbol(BLOGTokenConstants.NEQ); }
"&"			{ return symbol(BLOGTokenConstants.AND); }
"|"     { return symbol(BLOGTokenConstants.OR); }
"!"			{ return symbol(BLOGTokenConstants.NOT); }
"->"		{ return symbol(BLOGTokenConstants.RIGHTARROW);}
"="			{ return symbol(BLOGTokenConstants.EQ); }
"~"			{ return symbol(BLOGTokenConstants.DISTRIB); }
"#"     { return symbol(BLOGTokenConstants.NUMSIGN); }

/* seperator */
"("			{ return symbol(BLOGTokenConstants.LPAREN); }
")"			{ return symbol(BLOGTokenConstants.RPAREN); }
"}"			{ return symbol(BLOGTokenConstants.RBRACE); }
"{"			{ return symbol(BLOGTokenConstants.LBRACE); }
"["			{ return symbol(BLOGTokenConstants.LBRACKET); }
"]"			{ return symbol(BLOGTokenConstants.RBRACKET); }
";"			{ return symbol(BLOGTokenConstants.SEMI); }
":"			{ return symbol(BLOGTokenConstants.COLON);}
"."     { return symbol(BLOGTokenConstants.DOT); }
","			{ return symbol(BLOGTokenConstants.COMMA);}


 /* comments */
{Comment} { /* ignore */ }

{Whitespace} { /* Do nothing */}

{Identifier} {return symbol(BLOGTokenConstants.ID, yytext()); }

[A-Za-z][A-Za-z0-9_]*([.][A-Za-z][A-Za-z0-9_]*)* {
        return symbol(BLOGTokenConstants.CLASS_NAME, yytext()); }
}

<STR_LIT>\" { /* closing double-quote not matched by \" rule below */
       BLOGSymbol s =   symbol(BLOGTokenConstants.STRING_LITERAL, 
			       string_buf.toString());
       string_buf = new StringBuffer(); /* reinitialize the buffer */
       yybegin(YYINITIAL);
       return s;}

<CHAR_LIT>\' { /* closing single-quote not matched by \' rule below */
       BLOGSymbol s;
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
       return symbol(BLOGTokenConstants.ERROR, 
                         "Unrecognized escape character: \'" 
                         + yytext() + "\'"); }

<STR_LIT,CHAR_LIT>{LineTerminator}  { 
       return symbol(BLOGTokenConstants.ERROR, 
                         "Line terminator in string or character literal."); }

<STR_LIT,CHAR_LIT>. { /* Char in quotes, not matched by any rule above */
       string_buf.append(yytext()); }


<YYINITIAL>.                     { return symbol(BLOGTokenConstants.ERROR, 
                                          yytext()); }
.             { /*
                    *  This should be the very last rule and will match
                    *  everything not matched by other lexical rules.
                    */
                   System.err.println("LEXER BUG - UNMATCHED: " + yytext()); }
