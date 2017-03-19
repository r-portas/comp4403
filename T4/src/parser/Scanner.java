package parser;

import source.ErrorHandler;
import source.Errors;
import source.Source;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * class Scanner - hand coded lexical analyzer for PL0
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 * Tokenizes the requested input file or standard input. 
 * The tokens are defined in the enumeration Token.
 * Returns one token on each call to getNextToken()
 */
public class Scanner implements java.util.Iterator<LexicalToken>{
      private final static Map<String, Token> keywords;

      /* Static initializer */
      static {
          keywords = new HashMap<String, Token>();
          addKeyword( Token.KW_BEGIN );
          addKeyword( Token.KW_CALL );
          addKeyword( Token.KW_CONST );
          addKeyword( Token.KW_DO );
          addKeyword( Token.KW_ELSE );
          addKeyword( Token.KW_END );
          addKeyword( Token.KW_IF );
          addKeyword( Token.KW_PROCEDURE );
          addKeyword( Token.KW_READ );
          addKeyword( Token.KW_REPEAT );
          addKeyword( Token.KW_THEN );
          addKeyword( Token.KW_TYPE );
          addKeyword( Token.KW_UNTIL );
          addKeyword( Token.KW_VAR );
          addKeyword( Token.KW_WHILE );
          addKeyword( Token.KW_WRITE );
      }

      /** Size of the lookahead buffer */
      private static final int BUFFERSIZE = 16384;

      /*************** Instance Variables *****************/
      private Source source; /* The source handler used by this lexer */
      private char charBuffer[] = new char[BUFFERSIZE];
      private int nextCh; /* The one character of look-ahead */
      private int bufferPos = 0; /* Position in charBuffer */
      private int bufferLength = 0; /* Number of characters in buffer */
      private int currentLine = 0; /* Number of newlines encountered */
      private int currentColumn = 0; /* Character position in current line */
      private static Errors errors = ErrorHandler.getErrorHandler(); /* Error handler */

      /****************** Constructors ********************/
      /** Basic constructor
       * @param src input source program stream */
      public Scanner( Source src ) throws IOException {
          source = src;
          nextCh = getNextChar();
      }
      /** Constructor with file name argument
       * @param fileName input file containing source program */
      public Scanner( String fileName ) throws IOException {
          this( new Source(fileName) );
      }
      /******************* Public Methods *****************/
      /** @return the current source handler */
      public Source getSourceHandler() {
          return source;
      }
      /** Returns true unless at end of file. */
      public boolean hasNext() {
          return nextCh != -1;
      }
      /** Fetch the next token from the input stream. 
       * @return next token unless end of file is reached
       * in which case an EOF token is returned
       */
      public LexicalToken next() {
          Location currentLocation;
          char ch;
          /* Use a loop to allow multiple whitespace elements to be skipped.
           * When a token is matched it is returned,
           * but when a white space element is recognised 
           * this loop repeats to search for the next token or skip more
           * white space.
           */
          do {
              currentLocation = new Location( currentLine, currentColumn );
              // Check if we've hit end of file
              if( nextCh == -1 ) {
                  return new LexicalToken( Token.EOF, currentLocation );
              }
              ch = (char)nextCh;
              nextCh = getNextChar();
              /* If ch is a letter, read an identifier or keyword */
              if( Character.isLetter(ch) ) {
                  return getIdentifierToken( ch, currentLocation );
              }
              /* if ch is a digit, read a number */
              if( Character.isDigit(ch) ) {
                  return getNumberToken( ch, currentLocation );
              }
              switch( ch ) {
              // Skip over whitespace
              case ' ':   // blank
                  break;
              case '\t':  // tab
                  break;
              case '\f':  // form feed
                  break;
              case '\n':  // newline
                  currentLine++;
                  currentColumn = 0;
                  break;
              case '\r':  // carriage return
                  break;
              case '/':
                  if( nextCh == '/' ) {
                      // skip comment until end of line or end of file
                      while( nextCh != '\n' && nextCh != -1 ) {
                          nextCh = getNextChar();
                      }
                      // newline or end of file handled by next iteration
                      break;
                  } else {
                      /* We have a divide sign */
                      return new LexicalToken( Token.DIVIDE, currentLocation );
                  }
              case '+': 
                  return new LexicalToken( Token.PLUS, currentLocation );
              case '-':
                  return new LexicalToken( Token.MINUS, currentLocation );
              case '*': 
                  return new LexicalToken( Token.TIMES, currentLocation );
              case '(': 
                  return new LexicalToken( Token.LPAREN, currentLocation );
              case ')': 
                  return new LexicalToken( Token.RPAREN, currentLocation );
              case ';': 
                  return new LexicalToken( Token.SEMICOLON, currentLocation );
              case ':':
                  if( nextCh == '=' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.ASSIGN, currentLocation );
                  }
                  return new LexicalToken( Token.COLON, currentLocation );
              case ',':
                  return new LexicalToken( Token.COMMA, currentLocation );
              case '.': 
                  if( nextCh == '.' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.RANGE, currentLocation);
                  }
                  return new LexicalToken( Token.ILLEGAL, currentLocation );
              case '=':
                  return new LexicalToken( Token.EQUALS, currentLocation );
              case '!':
                  if( nextCh == '=' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.NEQUALS, currentLocation );
                  }
                  return new LexicalToken( Token.LOG_NOT, currentLocation );
              case '<':
                  if( nextCh == '=' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.LEQUALS, currentLocation );
                  }
                  return new LexicalToken( Token.LESS, currentLocation );
              case '>':
                  if( nextCh == '=' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.GEQUALS, currentLocation );
                  }
                  return new LexicalToken( Token.GREATER, currentLocation );
              case '&':
                  if( nextCh == '&' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.LOG_AND, currentLocation );
                  }
                  return new LexicalToken( Token.ILLEGAL, currentLocation );  
              case '|':
                  if( nextCh == '|' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.LOG_OR, currentLocation );
                  }
                  return new LexicalToken( Token.ILLEGAL, currentLocation );
              case '[':
                  return new LexicalToken( Token.LBRACKET, currentLocation );
              case ']':
                  return new LexicalToken( Token.RBRACKET, currentLocation );
              default:
                  return new LexicalToken( Token.ILLEGAL, currentLocation );
              }
          } while ( true );                
      }
      /** The remove method is not supported by this class */
      public void remove() throws UnsupportedOperationException {
          throw new UnsupportedOperationException();
      }

      /** read an identifier (or keyword) starting from the given character ch,
       * and return the resulting token */
      private LexicalToken getIdentifierToken( char ch, Location currentLocation ) {
          StringBuffer buf = new StringBuffer();
          buf.append( ch );
          while( nextCh != -1 && Character.isLetterOrDigit((char)nextCh) ) {
              buf.append( (char)nextCh );
              nextCh = getNextChar();
          } 
          String word = buf.toString(); // .toLowerCase(); // Case insensitive
          if( keywords.containsKey( word ) ) {
              return new LexicalToken( keywords.get(word), currentLocation );
          } else {
              return new IdentifierToken( Token.IDENTIFIER, currentLocation, word );
          }
      }

      /** read a number starting from the given character ch and return the
       * resulting token */
      private LexicalToken getNumberToken( char ch, Location currentLocation ) {
          StringBuffer buf = new StringBuffer();
          buf.append( ch );
          while( nextCh != -1 && Character.isDigit((char)nextCh) ) {
              buf.append( (char)nextCh );
              nextCh = getNextChar();
          }
          int value = 0x80808080; // Nonsense value
          try {
              value = Integer.parseInt( buf.toString() );
          } catch( NumberFormatException e ) { 
              /* Can only happen if the number is too big */
              errors.error( "integer too large", currentLocation );
          }
          return new NumberToken( Token.NUMBER, currentLocation, value );
      }
      /* Fetch the next character from the input stream and return it, updating
       * the current position. 
       */
      private int getNextChar() {
          if( bufferPos == bufferLength ) {
              bufferPos = 0;
              try {
                  bufferLength = source.read( charBuffer, 0, charBuffer.length );
              } catch( IOException e ) {
                  errors.fatal( "Caught IOException " + e, ErrorHandler.NO_LOCATION );
                    /* Never returns, but just in case: */
                    System.exit(1);
              }
              if( bufferLength == -1 ) {
                  return -1;
              }
          }
          currentColumn++;
          return charBuffer[bufferPos++];
      }
      /** Add a keyword to the keyword look up table */
      private static void addKeyword( Token keyword ) {
          if( keywords.put( keyword.toString(), keyword ) != null) {
              errors.fatal( "duplicate keyword in scanner", ErrorHandler.NO_LOCATION );
          }
      }
}
