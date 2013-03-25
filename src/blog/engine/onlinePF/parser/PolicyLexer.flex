
package blog.engine.onlinePF.parser;
import java_cup.runtime.*;

%%

%class PolicyLexer
%implements ScannerWithLocInfo
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
    return yyline;
  }
  
  public int getCurColNum() {
    return yycolumn;
  }

  private String filename;

  public void setFilename(String fname) {
    filename = fname;
  }

  public String getCurFilename() {
    return filename;
  }
  
  
  private Symbol symbol(int type) {
    return symbol(type, null);
  }

  private Symbol symbol(int type, Object value) {
    return new PolicySymbol(type, getCurLineNum(), getCurColNum(), yychar, yychar+yylength(), value);

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
      return symbol(PolicyTokenConstants.error, 
                        "File ended before string or character literal "
                        + "was terminated.");

  }
  /* Reinitialize everything before signaling EOF */
  string_buf = new StringBuffer();
  yybegin(YYINITIAL);
  return symbol(PolicyTokenConstants.EOF);
%eofval}


Alpha = [A-Za-z]

Digit = [0-9]


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
[Ee][Ll][Ss][Ee]  	{ return symbol(PolicyTokenConstants.ELSE); }
[Ee][Ll][Ss][Ee][Ii][Ff]  { return symbol(PolicyTokenConstants.ELSEIF); }
[Ii][Ff]  		{ return symbol(PolicyTokenConstants.IF); }


/* literals */
"true"	{ return symbol(PolicyTokenConstants.BOOLEAN_LITERAL, new Boolean(true)); }
"false"   { return symbol(PolicyTokenConstants.BOOLEAN_LITERAL, new Boolean(false)); }
"null" {return symbol(PolicyTokenConstants.NULL); }
{IntegerLiteral}  { return symbol(PolicyTokenConstants.INT_LITERAL, new Integer(yytext())); }
{DoubleLiteral} { return 
		 symbol(PolicyTokenConstants.DOUBLE_LITERAL, new Double(yytext())); }

/* operators */
"<"     { return symbol(PolicyTokenConstants.LT); }
">"     { return symbol(PolicyTokenConstants.GT); }
"<="    { return symbol(PolicyTokenConstants.LEQ); }
">="    { return symbol(PolicyTokenConstants.GEQ); }
"=="		{ return symbol(PolicyTokenConstants.EQEQ); }
"!="    { return symbol(PolicyTokenConstants.NEQ); }


/* seperator */
"("			{ return symbol(PolicyTokenConstants.LPAREN); }
")"			{ return symbol(PolicyTokenConstants.RPAREN); }
"}"			{ return symbol(PolicyTokenConstants.RBRACE); }
"{"			{ return symbol(PolicyTokenConstants.LBRACE); }
";"			{ return symbol(PolicyTokenConstants.SEMI); }

 /* comments */
{Comment} { /* ignore */ }

{Whitespace} { /* Do nothing */}
}

<STR_LIT>\" { /* closing double-quote not matched by \" rule below */
       Symbol s =   symbol(PolicyTokenConstants.STRING_LITERAL, 
			       string_buf.toString());
       string_buf = new StringBuffer(); /* reinitialize the buffer */
       yybegin(YYINITIAL);
       return s;}


<STR_LIT>\\b  { string_buf.append('\b'); }
<STR_LIT>\\t  { string_buf.append('\t'); }
<STR_LIT>\\n  { string_buf.append('\n'); }
<STR_LIT>\\f  { string_buf.append('\f'); }
<STR_LIT>\\r  { string_buf.append('\r'); }
<STR_LIT>\\\" { string_buf.append('\"'); }
<STR_LIT>\\\' { string_buf.append('\''); }
<STR_LIT>\\\\ { string_buf.append('\\'); }

<STR_LIT>\\{OCTAL_DIGIT} 
       { int code = Integer.parseInt(yytext().substring(1), 8);
         string_buf.append((char) code); }
<STR_LIT>\\{OCTAL_DIGIT}{OCTAL_DIGIT} 
       { int code = Integer.parseInt(yytext().substring(1), 8);
         string_buf.append((char) code); }
<STR_LIT>\\{ZERO_TO_THREE}{OCTAL_DIGIT}{OCTAL_DIGIT} 
       { int code = Integer.parseInt(yytext().substring(1), 8);
         string_buf.append((char) code); }

<STR_LIT>\\u{HEX_DIGIT}{HEX_DIGIT}{HEX_DIGIT}{HEX_DIGIT} { 
       int code = Integer.parseInt(yytext().substring(2), 16);
       string_buf.append((char) code); }

<STR_LIT>\\.  { 
       return symbol(PolicyTokenConstants.error, 
                         "Unrecognized escape character: \'" 
                         + yytext() + "\'"); }

<STR_LIT>{LineTerminator}  { 
       return symbol(PolicyTokenConstants.error, 
                         "Line terminator in string or character literal."); }

<STR_LIT>. { /* Char in quotes, not matched by any rule above */
       string_buf.append(yytext()); }


<YYINITIAL>.  { return symbol(PolicyTokenConstants.error, 
                                          yytext()); }
