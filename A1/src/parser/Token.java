package parser;

/** enumeration Token - Defines the basic tokens returned from the lexical analyzer.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 */
public enum Token {
    EOF( "End-of-file"),
    PLUS( "+" ),
    MINUS( "-" ),
    TIMES( "*"),
    DIVIDE( "/" ),
    LPAREN( "(" ),
    RPAREN( ")" ),
    LBRACKET( "[" ),
    RBRACKET( "]" ),
    SEMICOLON( ";" ),
    COLON( ":" ),
    ASSIGN( ":=" ),
    COMMA( "," ),
    RANGE( ".." ),
    EQUALS( "=" ),
    NEQUALS( "!=" ),
    LEQUALS( "<=" ),
    LESS( "<" ),
    GEQUALS( ">=" ),
    GREATER( ">" ),
    LOG_AND( "&&" ),
    LOG_OR( "||" ),
    LOG_NOT( "!" ),
    BAR( "|" ),
    KW_BEGIN( "begin" ),
    KW_CALL( "call" ),
    KW_CASE( "case" ),
    KW_CONST( "const" ),
    KW_DEFAULT( "default" ),
    KW_DO( "do" ),
    KW_ELSE( "else" ),
    KW_END( "end" ),
    KW_IF( "if" ),
    KW_OF( "of" ),
    KW_PROCEDURE( "procedure" ),
    KW_READ( "read" ),
    KW_SKIP( "skip" ),
    KW_THEN( "then" ),
    KW_TYPE( "type" ),
    KW_VAR( "var" ),
    KW_WHEN( "when" ),
    KW_WHILE( "while" ),
    KW_WRITE( "write" ),
    IDENTIFIER( "identifier" ),
    NUMBER( "number" ),
    ILLEGAL( "illegal" );
    
    /** The name of the token */
    String name;
    
    private Token( String name ) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
}
