package source;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * class CompilerError -  Represents a single error.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 * An error can consist of 
 * - an error message string,
 * - the severity, and
 * - the location in the source input of the error.
 * If no location can be assigned to the error then
 * ErrorHandler.NO_LOCATION is used.
 * @see pl0.source.Severity
 */
public class CompileError implements Comparable<CompileError> {
    /** The error message */
    private String message;
    /** The error's severity */
    private Severity severity;
    /** The location in the input source, or NO_LOCATION */
    private Location location;

    public CompileError( String message, Severity severity, Location loc ) {
        this.message = message;
        this.severity = severity;
        this.location = loc;
    }

    /** Ordering of errors is based on their location.
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo( CompileError that ) {
        if( location.getLine() < that.getLocation().getLine() ) {
            return -1;
        } else if( location.getLine() == that.getLocation().getLine() ) {
            if (location.getColumn() < that.getLocation().getColumn()) {
                return -1;
            } else if (location.getColumn() == that.getLocation().getColumn()) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }
    public Location getLocation() {
        return location;
    }
    public Severity getSeverity() {
        return severity;
    }
    public String toString() {
        return severity.toString() + ": " + message;
    }
}
