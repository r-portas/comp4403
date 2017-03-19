package parser;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * class NumberToken - Number token needs an integer value
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */ 
public class NumberToken extends LexicalToken {

    private int intValue;

    /** Construct a token with the given type, location and integer value. 
     * @param type should always be NUMBER.
     */
    public NumberToken( Token type, Location loc, int intValue ) {
        super(type,loc);
        this.intValue = intValue;
    }
    /** Extract integer value of NUMBER token */
    @Override
    public int getIntValue( ) {
        return intValue;
    }
    @Override
    public String toString() {
        return "number(" + intValue + ")";
    }
}
