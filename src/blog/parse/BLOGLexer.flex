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

%%

%class BLOGLexer
%cup
%unicode
%line
%char
%column
%public

%{
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
      error("File ended before string or character literal "
                        + "was terminated.");
  }
  /* Reinitialize everything before signaling EOF */
  string_buf = new StringBuffer();
  yybegin(YYINITIAL);
  return symbol(BLOGTokenConstants.EOF);
%eofval}


Alpha = [A-Za-z]

Digit = [0-9]

Identifier = ({Alpha}|_)({Alpha}|{Digit}|_)*

IntegerLiteral = [+-]?{Digit}+

FLit1    = {Digit}+ \. {Digit}* 
FLit2    = \. {Digit}+ 
FLit3    = {Digit}+ 
Exponent = [eE] [+-]? {Digit}+
DoubleLiteral = [+-]?({FLit1}|{FLit2}|{FLit3}) {Exponent}?

LineTerminator	= [\n\r\r\n]

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
[Nn][Oo][Nn][Rr][Aa][Nn][Dd][Oo][Mm] { return symbol(BLOGTokenConstants.FIXED); }
[Ff][Ii][Xx][Ee][Dd] { return symbol(BLOGTokenConstants.FIXED); }			   
[Gg][Ee][Nn][Ee][Rr][Aa][Tt][Ii][Nn][Gg] { return symbol(BLOGTokenConstants.ORIGIN); }
[Oo][Rr][Ii][Gg][Ii][Nn] { return symbol(BLOGTokenConstants.ORIGIN); }
[Gg][Uu][Aa][Rr][Aa][Nn][Tt][Ee][Ee][Dd] { return symbol(BLOGTokenConstants.DISTINCT); }
[Dd][Ii][Ss][Tt][Ii][Nn][Cc][Tt] { return symbol(BLOGTokenConstants.DISTINCT); }
[Tt][Hh][Ee][Nn]     { return symbol(BLOGTokenConstants.THEN); }
[Ee][Ll][Ss][Ee]  	{ return symbol(BLOGTokenConstants.ELSE); }
[Ff][Oo][Rr]         { return symbol(BLOGTokenConstants.FOR); }
[Ii][Ff]  		{ return symbol(BLOGTokenConstants.IF); }
[Qq][Uu][Ee][Rr][Yy]	{ return symbol(BLOGTokenConstants.QUERY);}
[Oo][Bb][Ss]         { return symbol(BLOGTokenConstants.OBS);}
[Pp][Aa][Rr][Aa][Mm] { return symbol(BLOGTokenConstants.PARAM);}
[Ee][Xx][Ii][Ss][Tt][Ss] { return symbol(BLOGTokenConstants.EXISTS); }
[Ff][Oo][Rr][Aa][Ll][Ll] { return symbol(BLOGTokenConstants.FORALL); }
[Ll][Ii][Ss][Tt] { return symbol(BLOGTokenConstants.LIST); }
[Mm][Aa][Pp] { return symbol(BLOGTokenConstants.MAP); }
[Dd][Ii][Ss][Tt][Rr][Ii][Bb][Uu][Tt][Ii][Oo][Nn] { return symbol(BLOGTokenConstants.DISTRIBUTION); }
	

/* literals */
"true"	{ return symbol(BLOGTokenConstants.BOOLEAN_LITERAL, new Boolean(true)); }
"false"   { return symbol(BLOGTokenConstants.BOOLEAN_LITERAL, new Boolean(false)); }
"null" {return symbol(BLOGTokenConstants.NULL); }
{IntegerLiteral}  { return symbol(BLOGTokenConstants.INT_LITERAL, new Integer(yytext())); }
{DoubleLiteral} { return 
		 symbol(BLOGTokenConstants.DOUBLE_LITERAL, new Double(yytext())); }

/* operators */
"+"     { return symbol(BLOGTokenConstants.PLUS); }
"-"     { return symbol(BLOGTokenConstants.MINUS); }
"*"     { return symbol(BLOGTokenConstants.MULT); }
"/"     { return symbol(BLOGTokenConstants.DIV); }
"%"     { return symbol(BLOGTokenConstants.MOD); }
"^"     { return symbol(BLOGTokenConstants.POWER); }
"<"     { return symbol(BLOGTokenConstants.LT); }
">"     { return symbol(BLOGTokenConstants.GT); }
"<="    { return symbol(BLOGTokenConstants.LEQ); }
">="    { return symbol(BLOGTokenConstants.GEQ); }
"=="		{ return symbol(BLOGTokenConstants.EQEQ); }
"!="    { return symbol(BLOGTokenConstants.NEQ); }
"&"			{ return symbol(BLOGTokenConstants.AND); }
"|"     { return symbol(BLOGTokenConstants.OR); }
"!"			{ return symbol(BLOGTokenConstants.NOT); }
"@"     { return symbol(BLOGTokenConstants.AT); }
"->"		{ return symbol(BLOGTokenConstants.RIGHTARROW); }
"=>"    { return symbol(BLOGTokenConstants.DOUBLERIGHTARROW); }


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
"="			{ return symbol(BLOGTokenConstants.EQ); }
"~"			{ return symbol(BLOGTokenConstants.DISTRIB); }
"#"     { return symbol(BLOGTokenConstants.NUMSIGN); }

 /* comments */
{Comment} { /* ignore */ }

{Whitespace} { /* Do nothing */}

{Identifier} {return symbol(BLOGTokenConstants.ID, yytext()); }
}

<STR_LIT>\" { /* closing double-quote not matched by \" rule below */
       Symbol s =   symbol(BLOGTokenConstants.STRING_LITERAL, 
			       string_buf.toString());
       string_buf = new StringBuffer(); /* reinitialize the buffer */
       yybegin(YYINITIAL);
       return s;}

<CHAR_LIT>\' { /* closing single-quote not matched by \' rule below */
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
       error("Unrecognized escape character: \'" 
                         + yytext() + "\'"); }

<STR_LIT,CHAR_LIT>{LineTerminator}  { 
       error("Line terminator in string or character literal."); }

<STR_LIT,CHAR_LIT>. { /* Char in quotes, not matched by any rule above */
       string_buf.append(yytext()); }


<YYINITIAL>.  { error("Lexer encountered some error"); }
