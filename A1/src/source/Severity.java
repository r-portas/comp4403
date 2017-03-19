package source;

/** 
 * enumeration Severity - Error message severity types 
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */
public enum Severity {
    FATAL( "Fatal" ),
    RESTRICTION( "Restriction" ),
    ERROR( " Error" ),
    WARNING( "Warning" ),
    REPAIR( "Repair" ),
    NOTE( "Note" ),
    INFORMATION( "Information" );
   
    String message;

    private Severity( String message ) {
        this.message = message;
    }
    public String toString() {
        return message;
    }
}
