package parser;

import java.util.EnumSet;

/** 
 * class TokenSet - Provides operations on sets of Tokens
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 * Provide operations to construct, union and test membership
 * of set of Tokens.
 */ 
public class TokenSet {

    private EnumSet<Token> set;

    /** Construct a new TokenSet from a list of tokens */
    public TokenSet( Token first, Token... rest ) {
        set = EnumSet.of( first, rest );
    }
    /** Construct a new TokenSet from an existing one */
    public TokenSet( TokenSet elems ) {
        set = EnumSet.copyOf( elems.set );
    }
    /** Construct a new TokenSet from the union of this and the other */
    public TokenSet union( TokenSet other ) {
        TokenSet result =  new TokenSet( other );
        result.set.addAll( set );
        return result;
    }
    /** Construct a new TokenSet from this plus one more Token */
    public TokenSet union( Token other ) {
        TokenSet result = new TokenSet( other );
        result.set.addAll( set );
        return result;
    }
    /** Construct a new TokenSet from this plus a list of Tokens */
    public TokenSet union( Token first, Token... rest ) {
        TokenSet result = new TokenSet( first, rest );
        result.set.addAll( set );
        return result;
    }
    /** Return whether a token is contained in the set */
    public boolean contains( Token token ) {
        return set.contains( token );
    }
    /** Convert set to string */
    @Override
    public String toString() {
        String m = "{ ";
        String sep = "";
        for( Token t: set ) {
            m += sep + "'" + t + "'";
            sep = ", ";
        }
        return m + " }";
    }
}
