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
[Tt][Yy][Pp][Ee] { return new Symbol(BLOGTokenConstants.TYPE); }
[Rr][Aa][Nn][Dd][Oo][Mm] { return new Symbol(BLOGTokenConstants.RANDOM); }
[Nn][Oo][Nn][Rr][Aa][Nn][Dd][Oo][Mm] { return new Symbol(BLOGTokenConstants.NONRANDOM); }
[Ff][Ii][Xx][Ee][Dd] { return new Symbol(BLOGTokenConstants.NONRANDOM); }			   
[Gg][Ee][Nn][Ee][Rr][Aa][Tt][Ii][Nn][Gg] { return new Symbol(BLOGTokenConstants.GENERATING); }
[Oo][Rr][Ii][Gg][Ii][Nn] { return new Symbol(BLOGTokenConstants.GENERATING); }
[Gg][Uu][Aa][Rr][Aa][Nn][Tt][Ee][Ee][Dd] { return new Symbol(BLOGTokenConstants.GUARANTEED); }
[Dd][Ii][Ss][Tt][Ii][Nn][Cc][Tt] { return new Symbol(BLOGTokenConstants.GUARANTEED); }
[Ff][Aa][Cc][Tt][Oo][Rr] { return new Symbol(BLOGTokenConstants.FACTOR); }
[Pp][Aa][Rr][Ff][Aa][Cc][Tt][Oo][Rr] { return new Symbol(BLOGTokenConstants.PARFACTOR); }
[Tt][Hh][Ee][Nn]     { return new Symbol(BLOGTokenConstants.THEN); }
[Ee][Ll][Ss][Ee]  	{ return new Symbol(BLOGTokenConstants.ELSE); }
[Ff][Oo][Rr]         { return new Symbol(BLOGTokenConstants.FOR); }
[Ee][Ll][Ss][Ee][Ii][Ff]  { return 
				       new Symbol(BLOGTokenConstants.ELSEIF); }
[Ii][Ff]  		{ return new Symbol(BLOGTokenConstants.IF); }
[Qq][Uu][Ee][Rr][Yy]	{ return new Symbol(BLOGTokenConstants.QUERY);}
[Oo][Bb][Ss]         { return new Symbol(BLOGTokenConstants.OBS);}
[Pp][Aa][Rr][Aa][Mm] { return new Symbol(BLOGTokenConstants.PARAM);}
[Ee][Xx][Ii][Ss][Tt][Ss] { /* existential quantifier */
	return new Symbol(BLOGTokenConstants.EXISTS); }
[Ff][Oo][Rr][Aa][Ll][Ll] { /* universal quantifier */
	return new Symbol(BLOGTokenConstants.FORALL); }
	

/* literals */
"true"	{ return new Symbol(BLOGTokenConstants.TRUE); }
"false"   { return new Symbol(BLOGTokenConstants.FALSE); }
"null" {return new Symbol(BLOGTokenConstants.NULL); }
{DoubleLiteral} { return 
				    new Symbol(BLOGTokenConstants.DOUBLE_CONST,
					                           new Double(yytext())); }
{IntegerLiteral}  { /* Integers */
                       return new Symbol(BLOGTokenConstants.INT_CONST,
					                           new Integer(yytext())); }
{TimeLiteral} { return new Symbol(BLOGTokenConstants.TIME_CONST, 
				       yytext()); }

/* operators */
"+"     { return new Symbol(BLOGTokenConstants.PLUS); }
"-"     { return new Symbol(BLOGTokenConstants.MINUS); }
"*"     { return new Symbol(BLOGTokenConstants.MULT); }
"/"     { return new Symbol(BLOGTokenConstants.DIV); }
"%"     { return new Symbol(BLOGTokenConstants.MOD); }
"<"     { return new Symbol(BLOGTokenConstants.LT); }
">"     { return new Symbol(BLOGTokenConstants.GT); }
"<="    { return new Symbol(BLOGTokenConstants.LEQ); }
">="    { return new Symbol(BLOGTokenConstants.GEQ); }
"=="		{ return new Symbol(BLOGTokenConstants.EQ); }
"!="    { return new Symbol(BLOGTokenConstants.NEQ); }
"&"			{ return new Symbol(BLOGTokenConstants.AND); }
"|"     { return new Symbol(BLOGTokenConstants.OR); }
"!"			{ return new Symbol(BLOGTokenConstants.NEG); }
"->"		{ return new Symbol(BLOGTokenConstants.RIGHTARROW);}
"="			{ return new Symbol(BLOGTokenConstants.ASSIGN); }
"~"			{ return new Symbol(BLOGTokenConstants.DISTRIB); }
"#"     { return new Symbol(BLOGTokenConstants.NUMSIGN); }

/* seperator */
"("			{ return new Symbol(BLOGTokenConstants.LPAREN); }
")"			{ return new Symbol(BLOGTokenConstants.RPAREN); }
"}"			{ return new Symbol(BLOGTokenConstants.RBRACE); }
"{"			{ return new Symbol(BLOGTokenConstants.LBRACE); }
"["			{ return new Symbol(BLOGTokenConstants.LBRACKET); }
"]"			{ return new Symbol(BLOGTokenConstants.RBRACKET); }
"<"     { return new Symbol(BLOGTokenConstants.LANGLE); }
">"     { return new Symbol(BLOGTokenConstants.RANGLE); }
";"			{ return new Symbol(BLOGTokenConstants.SEMI); }
":"			{ return new Symbol(BLOGTokenConstants.COLON);}
"."     { return new Symbol(BLOGTokenConstants.DOT); }
","			{ return new Symbol(BLOGTokenConstants.COMMA);}


 /* comments */
{Comment} { /* ignore */ }

{Whitespace} { /* Do nothing */}

{Identifier} {return new Symbol(BLOGTokenConstants.ID, yytext()); }

[A-Za-z][A-Za-z0-9_]*([.][A-Za-z][A-Za-z0-9_]*)* {
        return new Symbol(BLOGTokenConstants.CLASS_NAME, yytext()); }
}

<STR_LIT>\" { /* closing double-quote not matched by \" rule below */
       Symbol s =   new Symbol(BLOGTokenConstants.STR_CONST, 
			       string_buf.toString());
       string_buf = new StringBuffer(); /* reinitialize the buffer */
       yybegin(YYINITIAL);
       return s;}

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


<YYINITIAL>.                     { return new Symbol(BLOGTokenConstants.ERROR, 
                                          yytext()); }
.             { /*
                    *  This should be the very last rule and will match
                    *  everything not matched by other lexical rules.
                    */
                   System.err.println("LEXER BUG - UNMATCHED: " + yytext()); }
