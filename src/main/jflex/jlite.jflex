package jelek;

import java_cup.runtime.*;

%%

%class Scanner
%unicode
%line
%column
%cup

%{
  StringBuffer sb = new StringBuffer();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline + 1, yycolumn + 1);
  }

  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }
%}


Newline = \R
Whitespace = \s
Comment = {BlockComment} | {LineComment}
BlockComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
LineComment = "//" .* {Newline}?

ClassName = [:uppercase:]\w*
Id = [:lowercase:]\w*
Integer = \d+

%state STRING

%%

<YYINITIAL> {
  /* keywords */
  "class" { return symbol(sym.CLASS); }
  "main" { return symbol(sym.MAIN); }
  "return" { return symbol(sym.RETURN); }
  "if" { return symbol(sym.IF); }
  "else" { return symbol(sym.ELSE); }
  "while" { return symbol(sym.WHILE); }
  "readln" { return symbol(sym.READLN); }
  "println" { return symbol(sym.PRINTLN); }
  "null" { return symbol(sym.NULL); }
  "this" { return symbol(sym.THIS); }
  "new" { return symbol(sym.NEW); }

  /* types */
  "Int" { return symbol(sym.INT); }
  "Bool" { return symbol(sym.BOOL); }
  "String" { return symbol(sym.STRING); }
  "Void" { return symbol(sym.VOID); }
  {ClassName} { return symbol(sym.CNAME, yytext()); }

  /* separators */
  "(" { return symbol(sym.LPAREN); }
  ")" { return symbol(sym.RPAREN); }
  "{" { return symbol(sym.LBRACE); }
  "}" { return symbol(sym.RBRACE); }
  "," { return symbol(sym.COMMA); }
  ";" { return symbol(sym.SEMICOLON); }

  /* operators */
  "." { return symbol(sym.DOT); }
  "=" { return symbol(sym.ASSIGN); }
  "+" { return symbol(sym.PLUS); }
  "-" { return symbol(sym.MINUS); }
  "*" { return symbol(sym.STAR); }
  "/" { return symbol(sym.SLASH); }
  "<" { return symbol(sym.LT); }
  "<=" { return symbol(sym.LEQ); }
  ">" { return symbol(sym.GT); }
  ">=" { return symbol(sym.GEQ); }
  "==" { return symbol(sym.EQ); }
  "!=" { return symbol(sym.NEQ); }
  "!" { return symbol(sym.NOT); }
  "||" { return symbol(sym.OR); }
  "&&" { return symbol(sym.AND); }

  /* whitespace */
  {Comment} { /* ignore */ }
  {Whitespace} { /* ignore */ }

  /* literals */
  {Id} { return symbol(sym.ID, yytext()); }
  {Integer} { return symbol(sym.INTEGER_LITERAL, Integer.parseInt(yytext())); }
}

<STRING> {
  \"                             { yybegin(YYINITIAL);
                                   return symbol(sym.STRING_LITERAL,
                                   sb.toString()); }
  [^\n\r\"\\]+                   { sb.append( yytext() ); }
  \\t                            { sb.append('\t'); }
  \\n                            { sb.append('\n'); }
  \\r                            { sb.append('\r'); }
  \\\"                           { sb.append('\"'); }
  \\                             { sb.append('\\'); }
}

/* error fallback */
[^] { throw new RuntimeException("Illegal character \"" + yytext() +
                                 "\" at line " + (yyline+1) + ", column " + (yycolumn+1)); }
