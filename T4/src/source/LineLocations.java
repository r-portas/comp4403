package source;

import java_cup.runtime.ComplexSymbolFactory.Location;

import java.util.ArrayList;
import java.util.List;

/** 
 * class LineLocations - tracks the locations of lines within text file.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */

public class LineLocations {

    private List<Integer> lineEnds;
    
    LineLocations() {
        this.lineEnds = new ArrayList<>();
        this.lineEnds.add( -1 );
    }
    /** Add an end-of-line location.
     * @requires the new location greater than or equal to previous last location.
     */
    void add( int p ) {
        assert endLast().compareTo( p ) <= 0;
        // Add line only if nonempty
        if( endLast().compareTo( p ) != 0 ) {
            lineEnds.add( p );
        }
    }
    /** Retrieve the line number on which the given location occurs.
     * @requires the location is not greater than the end of the last line.
     */
    int getLineNumber( Location p ) {
        if (lineEnds.size() == 1) {
            if (lineEnds.get(0) != -1) {
                assert p.getColumn() <= lineEnds.get(p.getLine());
            }
        } else {
            assert p.getColumn() <= (lineEnds.get(p.getLine()) - lineEnds.get(p.getLine() - 1));
        }
        return p.getLine();
    }
    /** Get the location of the start of the line that contains location p.
     */
    Integer getLineStart( Location p ) {
        int endPrevious = lineEnds.get( p.getLine() );
        return endPrevious + 1;
    }
    /** Get the offset of location p from the start of the line on which
     * it occurs.
     */
    int offset( Location p ) {
        return p.getColumn();
    }
    /** Get the location of the end of the last line. */
    Integer endLast() {
        return lineEnds.get( lineEnds.size() - 1 );
    }
}
