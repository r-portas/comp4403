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
    /** Current scope */ 
    private Scope currentScope;

    /** Construct a symbol table and build the predefined scope
     * as its initial scope.
     */
    public SymbolTable() {
        super();
        SymEntry.ProcedureEntry predefined = 
                new SymEntry.ProcedureEntry("<predefined>", 
                ErrorHandler.NO_LOCATION, null );
        currentScope = new Scope( null, 0, predefined );
        predefined.setLocalScope( currentScope );
        Predefined.addPredefinedEntries( this );
    }
    /** Enter a new scope */
    public Scope newScope( SymEntry.ProcedureEntry procEntry ) {
        currentScope = new Scope( currentScope, currentScope.getLevel()+1, 
                procEntry );
        return currentScope;
    }
    /** Re-enter a scope on a traversal */
    public Scope reenterScope( Scope newScope ) {
        currentScope = newScope;
        return currentScope;
    }
    /** Exit scope */
    public void leaveScope() {
        currentScope = currentScope.getParent();
    }
    /** @return the level of the current scope: predefined scope is at level 0,
     *         main program scope is at level 1, and so on. 
     */
    public int getLevel() {
        return currentScope.getLevel();
    }
    public Scope getCurrentScope() {
        return currentScope;
    }
    /** Dump the context of the symbol table */
    @Override
    public String toString() {
        String s = "Symbol Table";
        Scope scope = currentScope;
        do {
            s += "\n" + scope;
            scope = scope.getParent();
        } while ( scope != null );
        return s;
    }
}
