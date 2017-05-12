package syms;
import machine.StackMachine;
import source.ErrorHandler;
import junit.framework.TestCase;
import java_cup.runtime.ComplexSymbolFactory.Location;
import syms.SymEntry.ProcedureEntry;

/**
 * class SymbolTableTest - Junit test for SymbolTable
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 */
public class SymbolTableTest extends TestCase {

    public SymbolTableTest(String arg0) {
        super(arg0);
    }
    
    private SymbolTable symtab;
    private Scope currentScope;
    private ProcedureEntry one;
    private ProcedureEntry two;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        symtab = new SymbolTable();
        currentScope = symtab.getPredefinedScope();
        ProcedureEntry test = 
            new ProcedureEntry( "test", ErrorHandler.NO_LOCATION );
        currentScope = currentScope.newScope( test );
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        symtab = null;
    }
    /* 
     * Test SymbolTable constructor.
     */
    public void testSymbolTable() throws Exception {
        assertEquals( 1, currentScope.getLevel() );
        assertEquals( Predefined.INTEGER_TYPE, 
                currentScope.lookupType( "int" ).getType() );
        assertEquals( Predefined.BOOLEAN_TYPE, 
                currentScope.lookupType( "boolean" ).getType() );
        assertEquals( Predefined.BOOLEAN_TYPE,
                currentScope.lookupConstant( "true" ).getType() );
        assertEquals( Predefined.BOOLEAN_TYPE,
                currentScope.lookupConstant( "false" ).getType() );
        assertEquals( 1, currentScope.lookupConstant( "true" ).getValue() );
        assertEquals( 0, currentScope.lookupConstant( "false" ).getValue() );
    }
    
    private void checkEntry( SymEntry e, String id, int level, Type type ) {
        assertEquals( id, e.getIdent() );
        assertEquals( level, e.getLevel() );
        assertEquals( type, e.getType() );
    }

    private void checkConstant( SymEntry.ConstantEntry e, String id, int level, 
            Type type, int value ) {
        checkEntry( e, id, level, type );
        assertEquals( value, e.getValue() );
        assertEquals( null, currentScope.lookupType( id ) );
        assertEquals( null, currentScope.lookupVariable( id ) );
        assertEquals( null, currentScope.lookupProcedure( id ) );
    }
    private void checkType( SymEntry.TypeEntry e, String id, int level, Type type ) {
        checkEntry( e, id, level, type );
        assertEquals( null, currentScope.lookupConstant( id ) );
        assertEquals( null, currentScope.lookupVariable( id ) );
        assertEquals( null, currentScope.lookupProcedure( id ) );
    }
    private void checkVariable( SymEntry.VarEntry e, String id, int level, Type type,
            int offset, int varSpace ) {
        checkEntry( e, id, level, type );
        assertEquals( StackMachine.LOCALS_BASE + offset, e.getOffset() );
        assertEquals( varSpace, currentScope.getVariableSpace() );
        assertEquals( null, currentScope.lookupType( id ) );
        assertEquals( null, currentScope.lookupConstant( id ) );
        assertEquals( null, currentScope.lookupProcedure( id ) );
    }
    private void checkProcedure( SymEntry.ProcedureEntry e, String id, int level ) {
        assertEquals( id, e.getIdent() );
        assertEquals( level, e.getLevel() );
        assertEquals( null, currentScope.lookupType( id ) );
        assertEquals( null, currentScope.lookupVariable( id ) );
        assertEquals( null, currentScope.lookupConstant( id ) );
    }
    /*
     * Test method for 'pl0.symbol_table.SymbolTable.get(String)'
     */
    public void testGet() {
        assertEquals( null, currentScope.lookupConstant( "e" ) );
        currentScope.addConstant( "e", new Location(0,0), Predefined.INTEGER_TYPE, 42 );
        SymEntry.ConstantEntry e1 = currentScope.lookupConstant( "e" );
        checkConstant( e1, "e", 1, Predefined.INTEGER_TYPE, 42 );

        one = new ProcedureEntry( "one", ErrorHandler.NO_LOCATION );
        currentScope = currentScope.newScope( one );
        currentScope.addConstant( "e", new Location(0,0), Predefined.BOOLEAN_TYPE, 0 );
        SymEntry.ConstantEntry e2 = currentScope.lookupConstant( "e" );
        checkConstant( e2, "e", 2, Predefined.BOOLEAN_TYPE, 0 );

        two = new ProcedureEntry( "two", ErrorHandler.NO_LOCATION );
        currentScope = currentScope.newScope( two );
        currentScope.addConstant( "e", new Location(0,0), Predefined.INTEGER_TYPE, 27 );
        SymEntry.ConstantEntry e3 = currentScope.lookupConstant( "e" );
        checkConstant( e3, "e", 3, Predefined.INTEGER_TYPE, 27 );
        
        currentScope = currentScope.getParent();
        SymEntry.ConstantEntry e4 = currentScope.lookupConstant( "e" );
        assertEquals( e2, e4 );
    
        currentScope = currentScope.getParent();
        SymEntry.ConstantEntry e5 = currentScope.lookupConstant( "e" );
        assertEquals( e1, e5 );
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.enterScope(String)'
     */
    public void testEnterScope() {
        assertEquals( 1, currentScope.getLevel() );
        ProcedureEntry one = new ProcedureEntry( "one", ErrorHandler.NO_LOCATION );
        currentScope = currentScope.newScope( one );
        assertEquals( 2, currentScope.getLevel() );
        two = new ProcedureEntry( "two", ErrorHandler.NO_LOCATION );
        currentScope = currentScope.newScope( two );
        assertEquals( 3, currentScope.getLevel() );
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.leaveScope()'
     */
    public void testLeaveScope() {
        ProcedureEntry one = new ProcedureEntry( "one", ErrorHandler.NO_LOCATION );
        currentScope = currentScope.newScope( one );
        assertEquals( 2, currentScope.getLevel() );
        two = new ProcedureEntry( "two", ErrorHandler.NO_LOCATION );
        currentScope = currentScope.newScope( two );
        assertEquals( 3, currentScope.getLevel() );
        currentScope = currentScope.getParent();
        assertEquals( 2, currentScope.getLevel() );
        currentScope = currentScope.getParent();
        assertEquals( 1, currentScope.getLevel() );
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.getParameterSpace()'
     */
    public void testGetParameterSpace() {

    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.addConstant(String, int, Type, int)'
     */
    public void testAddConstant() {
        assertEquals( null, currentScope.lookupConstant( "e" ) );
        currentScope.addConstant( "e", new Location(0,0), Predefined.INTEGER_TYPE, 42 );
        SymEntry.ConstantEntry e = currentScope.lookupConstant( "e" );
        checkConstant( e, "e", 1, Predefined.INTEGER_TYPE, 42 );
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.addType(String, int, Type)'
     */
    public void testAddType() {
        assertEquals( null, currentScope.lookupType( "e" ) );
        currentScope.addType( "e", new Location(0,0), Predefined.INTEGER_TYPE );
        SymEntry.TypeEntry e = currentScope.lookupType( "e" );
        checkType( e, "e", 1, Predefined.INTEGER_TYPE );
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.addVariable(String, int, Type)'
     */
    public void testAddVariable() {
        Type.ReferenceType refInt =
            new Type.ReferenceType( 
                    new Type.IdRefType("int",currentScope, 
                            new Location(0,0) ) );
        assertEquals( null, currentScope.lookupVariable( "e" ) );
        currentScope.addVariable( "e", new Location(0,0), refInt );
        currentScope.resolveScope();
        SymEntry.VarEntry e = currentScope.lookupVariable( "e" );
        checkVariable( e, "e", 1, refInt, 0, 1 );
        
        currentScope.addVariable( "f", new Location(0,0), refInt );
        currentScope.resolveScope();
        SymEntry.VarEntry f = currentScope.lookupVariable( "f" );
        checkVariable( f, "f", 1, refInt, 1, 2 );
        
        currentScope.addVariable( "g", new Location(0,0), refInt );
        currentScope.resolveScope();
        SymEntry.VarEntry g = currentScope.lookupVariable( "g" );
        checkVariable( g, "g", 1, refInt, 2, 3 );
        
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.addProcedure(String, int)'
     */
    public void testAddProcedure() {
        assertEquals( null, currentScope.lookupProcedure( "e" ) );
        currentScope.addProcedure( "e", new Location(0,0) );
        SymEntry.ProcedureEntry e = currentScope.lookupProcedure( "e" );
        checkProcedure( e, "e", 1 );
    }

}
