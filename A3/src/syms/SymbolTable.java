package syms;

import source.ErrorHandler;

/** A SymbolTable represents a sequence of scopes, one for each nested static
 * level, i.e., procedure, main program or the predefined scope. 
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 * It provides operations to add identifiers, look up identifiers, add a new 
 * scope and exit a scope. Searching for an identifier in an SymbolTable 
 * starts at the current scope, but then if it is not found, the search 
 * proceeds to the next outer (parent) scope, and so on.
 */
public class SymbolTable {
    private Scope predefinedScope;
    /** Construct a symbol table and build the predefined scope
     * as its initial scope.
     */
    public SymbolTable() {
        super();
        SymEntry.ProcedureEntry predefined = 
                new SymEntry.ProcedureEntry("<predefined>", 
                        ErrorHandler.NO_LOCATION, null );
        predefinedScope = new Scope( null, 0, predefined );
        predefined.setLocalScope( predefinedScope );
        Predefined.addPredefinedEntries( predefinedScope );
    }
    /** Return the predefined scope */
    public Scope getPredefinedScope() {
        return predefinedScope;
    }
    /** Dump the context of the symbol table from provided scope upwards */
    public String toString( Scope scope ) {
        String s = "Symbol Table";
        do {
            s += "\n" + scope;
            scope = scope.getParent();
        } while ( scope != null );
        return s;
    }
}
