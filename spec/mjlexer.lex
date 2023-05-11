// IMPORT SECTION --------------------------------------------------------------------------------------------------



package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;



%%
// DIRECTIVES SECTION ----------------------------------------------------------------------------------------------



%{

	// Including information about token position
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	
	// Including information about token position
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}

%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}



%%
// REGULAR EXPRESSIONS SECTION -------------------------------------------------------------------------------------



// BLANK CHARACTERS
" " 							{ }
"\b" 							{ }
"\t" 							{ }
"\r\n" 							{ }
"\f" 							{ }

// KEYWORDS
"program"   					{ return new_symbol(sym.PROG, yytext());}
"break"							{ return new_symbol(sym.BREAK, yytext()); }
"class"							{ return new_symbol(sym.CLASS, yytext()); }
"else"							{ return new_symbol(sym.ELSE, yytext()); }
"const"							{ return new_symbol(sym.CONST, yytext()); }
"if"							{ return new_symbol(sym.IF, yytext()); }
"while"							{ return new_symbol(sym.WHILE, yytext()); }
"new"							{ return new_symbol(sym.NEW, yytext()); }
"print" 						{ return new_symbol(sym.PRINT, yytext()); }
"read"							{ return new_symbol(sym.READ, yytext()); }
"return" 						{ return new_symbol(sym.RETURN, yytext()); }
"void" 							{ return new_symbol(sym.VOID, yytext()); }
"extends"						{ return new_symbol(sym.EXTENDS, yytext()); }
"continue"						{ return new_symbol(sym.CONTINUE, yytext()); }
"map"							{ return new_symbol(sym.MAP, yytext()); }

// OPERATORS
"+" 							{ return new_symbol(sym.PLUS, yytext()); }
"-" 							{ return new_symbol(sym.MINUS, yytext()); }
"*" 							{ return new_symbol(sym.MUL, yytext()); }
"/" 							{ return new_symbol(sym.DIV, yytext()); }
"%" 							{ return new_symbol(sym.MOD, yytext()); }
"==" 							{ return new_symbol(sym.EQUALS, yytext()); }
"!=" 							{ return new_symbol(sym.NOTEQUALS, yytext()); }
">" 							{ return new_symbol(sym.GREATER, yytext()); }
">=" 							{ return new_symbol(sym.GREATEREQUALS, yytext()); }
"<" 							{ return new_symbol(sym.LESS, yytext()); }
"<=" 							{ return new_symbol(sym.LESSEQUALS, yytext()); }
"&&" 							{ return new_symbol(sym.AND, yytext()); }
"||" 							{ return new_symbol(sym.OR, yytext()); }
"=" 							{ return new_symbol(sym.ASSIGN, yytext()); }
"++" 							{ return new_symbol(sym.INC, yytext()); }
"--" 							{ return new_symbol(sym.DEC, yytext()); }
";" 							{ return new_symbol(sym.SEMI, yytext()); }
":" 							{ return new_symbol(sym.COLON, yytext()); }
"," 							{ return new_symbol(sym.COMMA, yytext()); }
"." 							{ return new_symbol(sym.DOT, yytext()); }
"(" 							{ return new_symbol(sym.LPAREN, yytext()); }
")" 							{ return new_symbol(sym.RPAREN, yytext()); }
"[" 							{ return new_symbol(sym.LSQUARE, yytext()); }
"]" 							{ return new_symbol(sym.RSQUARE, yytext()); }
"{" 							{ return new_symbol(sym.LBRACE, yytext()); }
"}"								{ return new_symbol(sym.RBRACE, yytext()); }
"=>"							{ return new_symbol(sym.ARROW, yytext()); }

// COMMENTS
"//" 							{ yybegin(COMMENT); }
<COMMENT> . 					{ yybegin(COMMENT); }
<COMMENT> "\r\n" 				{ yybegin(YYINITIAL); }

// TOKEN TYPES
"true" | "false"				{ return new_symbol(sym.BOOL, yytext()); }
'[a-z|A-Z]'						{ return new_symbol(sym.CHAR, Character.valueOf(yytext().charAt(1))); }
[0-9]+  						{ return new_symbol(sym.NUMBER, Integer.valueOf(yytext())); }
([a-z]|[A-Z])[a-zA-Z0-9_]* 		{ return new_symbol(sym.IDENT, yytext()); }

. 								{ System.err.println("Lexical error (" + yytext() + ") on line " + (yyline+1) + ", on column " + yycolumn); }
