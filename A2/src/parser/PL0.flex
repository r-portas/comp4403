/** JFlex lexical analyzer for PL0.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 * The input to JFlex consists of three sections:
 * - user code to be included directly in the generated class,
 * - options and definitions for JFlex, and
 * - lexical rules defining the tokens.
 * These sections are separated by lines containing just "%%"
 */

/* --------------------------Usercode Section------------------------ */

package parser;

import java_cup.runtime.*;
import source.ErrorHandler;

%%
/* -----------------Options and Declarations Section----------------- */

/* The name of the class JFlex will create will be Lexer.
 * Will write the code to the file Lexer.java.
 */
%class Lexer
%unicode

/* Make the resulting class public
 */
%public

/* Will switch to a CUP compatibility mode to interface with a CUP
 * generated parser.
 * The terminal symbols defined by CUP are placed in the class CUPToken.
 */
%cupsym CUPToken
%cup

/* The value returned at end of file.
 */
%eofval{
    return makeToken( CUPToken.EOF );
%eofval}

/* The current line number can be accessed with the variable yyline
 * and the current column number with the variable yycolumn.
 */
%line
%column

/* The current character position (starting from 0) in the input file
 * is maintained in the variable yychar.
 * Not used.
 * %char
 */

/* Declarations
 * Code between %{ and %}, both of which must be at the beginning of a
 * line, will be copied verbatim into the lexer class source.
 * Here one declares member variables and functions that are used inside
 * scanner actions.
 */
%{
    ComplexSymbolFactory sf;
    public Lexer(java.io.Reader in, ComplexSymbolFactory sf){
        this(in);
        this.sf = sf;
    }

    /** To create a new java_cup.runtime.Symbol.
     * @param kind is an integer code representing the token.
     * Note that CUP and JFlex use integers to represent token kinds.
     */
    private Symbol makeToken( int kind ) {
        /* Symbol takes the token kind, and the locations of the
         * leftmost and rightmost characters of the substring of the
         * input file that matched the token. 
         */
        // System.err.println( "Token " + yytext() + " " + kind );
        return sf.newSymbol( CUPToken.terminalNames[kind], kind, 
            new ComplexSymbolFactory.Location(yyline, yycolumn), 
            new ComplexSymbolFactory.Location(yyline, yycolumn + yylength()) );
    }
    /** Also creates a new java_cup.runtime.Symbol with information
     * about the current token, but this object has a value. 
     * @param kind is an integer code representing the token.
     * @param value is an arbitrary Java Object.
     * Below when tokens such as a NUMBER or IDENTIFIER are 
     * recognised they pass values which are respectively
     * of type Integer and String. The types of these values *must*
     * match their type as declared in the Terminals sections
     * of the CUP specification.
     */
    private Symbol makeToken(int kind, Object value) {
        // System.err.println( "Token " + yytext() + " " +kind );
        return sf.newSymbol( CUPToken.terminalNames[kind], kind, 
            new ComplexSymbolFactory.Location(yyline, yycolumn), 
            new ComplexSymbolFactory.Location(yyline, yycolumn + yylength()), 
            value );
    }
%}

/* Macro Declarations
 * These declarations are regular expressions that will be used 
 * in the Lexical Rules Section.
 * To reference a macro called "M" within a regular expression, use "{M}".
 */

/* A line terminator is a \r (carriage return), \n (line feed), \r\n or
 * \f (form feed). */
LineTerminator = \r|\n|\r\n|\f

/* White space is a line terminator, space, or tab (\t).
 * A list of characters enclosed in square brackets matches a single character
 * that is one of those in the list, e.g., [ac] matches either "a" or "c".
 */
WhiteSpace = {LineTerminator} | [ \t]

/* Any non-newline character can appear in a comment.
 * A list of characters in square brackets, but with an "^" at the start of
 * the list matches a single character not in the list, e.g., [^abc] matches
 * any single character except "a", "b", or "c".
 */
CommentCharacter = [^\r\n\f]

Digit   = [0-9]
Letter  = [a-zA-Z]

%%
/* ------------------------Lexical Rules Section---------------------- */

/* This section contains a list of regular expressions and actions.
 * The lexical analyser matches the longest string starting at the current 
 * point in the input source that matches one of the regular expressions.
 * If more than one regular expression matches the same (longest) string,
 * then the first regular expression in the list is used.
 * For example, the string "if" matches both the pattern for the keyword
 * KW_IF and an IDENTIFIER, but because the pattern for the keyword
 * precedes that for the identifier, KW_IF takes precedence. However,
 * the string "ifx" matches IDENTIFIER; it doesn't match the keyword KW_IF
 * because that wouldn't give the longest match.
 * 
 * The actions are in the form of Java code enclosed in braces.
 * The action is executed when the scanner matches the associated
 * regular expression. */

"("     { return makeToken( CUPToken.LPAREN ); }
")"     { return makeToken( CUPToken.RPAREN ); }
";"     { return makeToken( CUPToken.SEMICOLON ); }
":="    { return makeToken( CUPToken.ASSIGN ); }
":"     { return makeToken( CUPToken.COLON ); }
"+"     { return makeToken( CUPToken.PLUS ); }
"-"     { return makeToken( CUPToken.MINUS ); }
"*"     { return makeToken( CUPToken.TIMES ); }
"/"     { return makeToken( CUPToken.DIVIDE ); }
"="     { return makeToken( CUPToken.EQUALS ); }
"!="    { return makeToken( CUPToken.NEQUALS ); }
"<="    { return makeToken( CUPToken.LEQUALS ); }
">="    { return makeToken( CUPToken.GEQUALS ); }
"<"     { return makeToken( CUPToken.LESS ); }
">"     { return makeToken( CUPToken.GREATER ); }
".."    { return makeToken( CUPToken.RANGE ); }
"."     { return makeToken( CUPToken.PERIOD ); }
","     { return makeToken( CUPToken.COMMA ); }
"^"     { return makeToken( CUPToken.POINTER ); }
"{"     { return makeToken( CUPToken.LCURLY ); }
"}"     { return makeToken( CUPToken.RCURLY ); }
"["     { return makeToken( CUPToken.LBRACKET ); }
"]"     { return makeToken( CUPToken.RBRACKET ); }
"begin"     { return makeToken( CUPToken.KW_BEGIN ); }
"call"      { return makeToken( CUPToken.KW_CALL ); }
"const"     { return makeToken( CUPToken.KW_CONST ); }
"do"        { return makeToken( CUPToken.KW_DO ); }
"else"      { return makeToken( CUPToken.KW_ELSE ); }
"end"       { return makeToken( CUPToken.KW_END ); }
"if"        { return makeToken( CUPToken.KW_IF ); }
"new"       { return makeToken( CUPToken.KW_NEW ); }
"procedure" { return makeToken( CUPToken.KW_PROCEDURE ); }
"read"      { return makeToken( CUPToken.KW_READ ); }
"record"    { return makeToken( CUPToken.KW_RECORD ); }
"then"      { return makeToken( CUPToken.KW_THEN ); }
"type"      { return makeToken( CUPToken.KW_TYPE ); } 
"var"       { return makeToken( CUPToken.KW_VAR ); }
"while"     { return makeToken( CUPToken.KW_WHILE ); }
"write"     { return makeToken( CUPToken.KW_WRITE ); }

/* The rule for identifier must come after keywords to give the keywords
 * priority. */
{Letter}({Letter}|{Digit})*
    { return makeToken( CUPToken.IDENTIFIER, yytext() ); }

{Digit}+
    { int value = 0x80808080; // Nonsense value
      try {
          value = Integer.parseInt( yytext() );
      } catch( NumberFormatException e ) { 
          /* Can only happen if the number is too big */
          ErrorHandler.getErrorHandler().error(
            "integer too large", 
            new ComplexSymbolFactory.Location(yyline, yycolumn) );
      }
      return makeToken( CUPToken.NUMBER, new Integer( value ) );
    }

/* This consumes the comment but not any following line terminator
 * (exploiting the longest match property).
 * The line terminator is consumed by the WhiteSpace definition.
 * This is to ensure a comment terminated by EOF, that isn't immediately 
 * preceded by a line terminator, is recognised.
 */
"//"{CommentCharacter}*
    { /* ignore comment - an empty action causes the lexical analyser
       * to skip the matched characters in the input and then start
       * scanning for a token from the next character. */ }

{WhiteSpace} 
    { /* ignore white space */ }

/* Match any other character.
 * Let the parser deal with any illegal characters */
.           { return makeToken( CUPToken.ILLEGAL ); }
