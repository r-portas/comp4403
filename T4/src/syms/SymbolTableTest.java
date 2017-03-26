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
    private ProcedureEntry one;
    private ProcedureEntry two;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        symtab = new SymbolTable();
        ProcedureEntry test = 
            new ProcedureEntry( "test", ErrorHandler.NO_LOCATION );
        symtab.newScope( test );
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
        assertEquals( 1, symtab.getCurrentScope().getLevel() );
        assertEquals( Predefined.INTEGER_TYPE, 
                symtab.getCurrentScope().lookupType( "int" ).getType() );
        assertEquals( Predefined.BOOLEAN_TYPE, 
                symtab.getCurrentScope().lookupType( "boolean" ).getType() );
        assertEquals( Predefined.BOOLEAN_TYPE,
                symtab.getCurrentScope().lookupConstant( "true" ).getType() );
        assertEquals( Predefined.BOOLEAN_TYPE,
                symtab.getCurrentScope().lookupConstant( "false" ).getType() );
        assertEquals( 1, symtab.getCurrentScope().lookupConstant( "true" ).getValue() );
        assertEquals( 0, symtab.getCurrentScope().lookupConstant( "false" ).getValue() );
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
        assertEquals( null, symtab.getCurrentScope().lookupType( id ) );
        assertEquals( null, symtab.getCurrentScope().lookupVariable( id ) );
        assertEquals( null, symtab.getCurrentScope().lookupProcedure( id ) );
    }
    private void checkType( SymEntry.TypeEntry e, String id, int level, Type type ) {
        checkEntry( e, id, level, type );
        assertEquals( null, symtab.getCurrentScope().lookupConstant( id ) );
        assertEquals( null, symtab.getCurrentScope().lookupVariable( id ) );
        assertEquals( null, symtab.getCurrentScope().lookupProcedure( id ) );
    }
    private void checkVariable( SymEntry.VarEntry e, String id, int level, Type type,
            int offset, int varSpace ) {
        checkEntry( e, id, level, type );
        assertEquals( StackMachine.LOCALS_BASE + offset, e.getOffset() );
        assertEquals( varSpace, symtab.getCurrentScope().getVariableSpace() );
        assertEquals( null, symtab.getCurrentScope().lookupType( id ) );
        assertEquals( null, symtab.getCurrentScope().lookupConstant( id ) );
        assertEquals( null, symtab.getCurrentScope().lookupProcedure( id ) );
    }
    private void checkProcedure( SymEntry.ProcedureEntry e, String id, int level ) {
        assertEquals( id, e.getIdent() );
        assertEquals( level, e.getLevel() );
        assertEquals( null, symtab.getCurrentScope().lookupType( id ) );
        assertEquals( null, symtab.getCurrentScope().lookupVariable( id ) );
        assertEquals( null, symtab.getCurrentScope().lookupConstant( id ) );
    }
    /*
     * Test method for 'pl0.symbol_table.SymbolTable.get(String)'
     */
    public void testGet() {
        assertEquals( null, symtab.getCurrentScope().lookupConstant( "e" ) );
        symtab.getCurrentScope().addConstant( "e", new Location(0,0), Predefined.INTEGER_TYPE, 42 );
        SymEntry.ConstantEntry e1 = symtab.getCurrentScope().lookupConstant( "e" );
        checkConstant( e1, "e", 1, Predefined.INTEGER_TYPE, 42 );

        one = new ProcedureEntry( "one", ErrorHandler.NO_LOCATION );
        symtab.newScope( one );
        symtab.getCurrentScope().addConstant( "e", new Location(0,0), Predefined.BOOLEAN_TYPE, 0 );
        SymEntry.ConstantEntry e2 = symtab.getCurrentScope().lookupConstant( "e" );
        checkConstant( e2, "e", 2, Predefined.BOOLEAN_TYPE, 0 );

        two = new ProcedureEntry( "two", ErrorHandler.NO_LOCATION );
        symtab.newScope( two );
        symtab.getCurrentScope().addConstant( "e", new Location(0,0), Predefined.INTEGER_TYPE, 27 );
        SymEntry.ConstantEntry e3 = symtab.getCurrentScope().lookupConstant( "e" );
        checkConstant( e3, "e", 3, Predefined.INTEGER_TYPE, 27 );
        
        symtab.leaveScope();
        SymEntry.ConstantEntry e4 = symtab.getCurrentScope().lookupConstant( "e" );
        assertEquals( e2, e4 );
    
        symtab.leaveScope();
        SymEntry.ConstantEntry e5 = symtab.getCurrentScope().lookupConstant( "e" );
        assertEquals( e1, e5 );
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.enterScope(String)'
     */
    public void testEnterScope() {
        assertEquals( 1, symtab.getCurrentScope().getLevel() );
        ProcedureEntry one = new ProcedureEntry( "one", ErrorHandler.NO_LOCATION );
        symtab.newScope( one );
        assertEquals( 2, symtab.getCurrentScope().getLevel() );
        two = new ProcedureEntry( "two", ErrorHandler.NO_LOCATION );
        symtab.newScope( two );
        assertEquals( 3, symtab.getCurrentScope().getLevel() );
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.leaveScope()'
     */
    public void testLeaveScope() {
        ProcedureEntry one = new ProcedureEntry( "one", ErrorHandler.NO_LOCATION );
        symtab.newScope( one );
        assertEquals( 2, symtab.getCurrentScope().getLevel() );
        two = new ProcedureEntry( "two", ErrorHandler.NO_LOCATION );
        symtab.newScope( two );
        assertEquals( 3, symtab.getCurrentScope().getLevel() );
        symtab.leaveScope();
        assertEquals( 2, symtab.getCurrentScope().getLevel() );
        symtab.leaveScope();
        assertEquals( 1, symtab.getCurrentScope().getLevel() );
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
        assertEquals( null, symtab.getCurrentScope().lookupConstant( "e" ) );
        symtab.getCurrentScope().addConstant( "e", new Location(0,0), Predefined.INTEGER_TYPE, 42 );
        SymEntry.ConstantEntry e = symtab.getCurrentScope().lookupConstant( "e" );
        checkConstant( e, "e", 1, Predefined.INTEGER_TYPE, 42 );
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.addType(String, int, Type)'
     */
    public void testAddType() {
        assertEquals( null, symtab.getCurrentScope().lookupType( "e" ) );
        symtab.getCurrentScope().addType( "e", new Location(0,0), Predefined.INTEGER_TYPE );
        SymEntry.TypeEntry e = symtab.getCurrentScope().lookupType( "e" );
        checkType( e, "e", 1, Predefined.INTEGER_TYPE );
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.addVariable(String, int, Type)'
     */
    public void testAddVariable() {
        Type.ReferenceType refInt =
            new Type.ReferenceType( 
                    new Type.IdRefType("int",symtab.getCurrentScope(), 
                            new Location(0,0) ) );
        assertEquals( null, symtab.getCurrentScope().lookupVariable( "e" ) );
        symtab.getCurrentScope().addVariable( "e", new Location(0,0), refInt );
        symtab.getCurrentScope().resolveScope();
        SymEntry.VarEntry e = symtab.getCurrentScope().lookupVariable( "e" );
        checkVariable( e, "e", 1, refInt, 0, 1 );
        
        symtab.getCurrentScope().addVariable( "f", new Location(0,0), refInt );
        symtab.getCurrentScope().resolveScope();
        SymEntry.VarEntry f = symtab.getCurrentScope().lookupVariable( "f" );
        checkVariable( f, "f", 1, refInt, 1, 2 );
        
        symtab.getCurrentScope().addVariable( "g", new Location(0,0), refInt );
        symtab.getCurrentScope().resolveScope();
        SymEntry.VarEntry g = symtab.getCurrentScope().lookupVariable( "g" );
        checkVariable( g, "g", 1, refInt, 2, 3 );
        
    }

    /*
     * Test method for 'pl0.symbol_table.SymbolTable.addProcedure(String, int)'
     */
    public void testAddProcedure() {
        assertEquals( null, symtab.getCurrentScope().lookupProcedure( "e" ) );
        symtab.getCurrentScope().addProcedure( "e", new Location(0,0) );
        SymEntry.ProcedureEntry e = symtab.getCurrentScope().lookupProcedure( "e" );
        checkProcedure( e, "e", 1 );
    }

}
