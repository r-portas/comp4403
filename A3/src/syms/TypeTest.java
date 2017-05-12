package syms;

import source.ErrorHandler;
import java_cup.runtime.ComplexSymbolFactory.Location;
import tree.Operator;
import tree.ConstExp;
import tree.ExpNode;
import syms.Type.IncompatibleTypes;
import junit.framework.TestCase;

/**
 * class TestType - JUnit test for class Type.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 */
public class TypeTest extends TestCase {

    public TypeTest(String arg0) {
        super(arg0);
    }

    Type et;
    Type.ScalarType it;
    Type.ScalarType bt;
    Type.ProcedureType pt;
    Type.SubrangeType ist;
    Type.SubrangeType bst;
    Type.SubrangeType isst;
    Operator addop;
    Operator eqop;
    Type.ReferenceType rit;
    Type.ProductType iit;
    Type.ProductType bbt;
    Type.FunctionType iiit;
    Type.FunctionType iibt;
    Type.FunctionType bbbt;
    ExpNode.ConstNode ix;
    ExpNode.VariableNode ivx;
    ExpNode.NarrowSubrangeNode isx;
    ExpNode.DereferenceNode rix;
    SymEntry.VarEntry iv;   
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Location noLoc = ErrorHandler.NO_LOCATION;
        ErrorHandler.getErrorHandler();
        SymbolTable symtab = new SymbolTable();
        Scope currentScope = symtab.getPredefinedScope();
        et = Type.ERROR_TYPE;
        it = Predefined.INTEGER_TYPE;
        bt = Predefined.BOOLEAN_TYPE;
        pt = new Type.ProcedureType().resolveType(null);
        ist = new Type.SubrangeType(  
                new ConstExp.NumberNode( ErrorHandler.NO_LOCATION, 
                        currentScope, it, 3),
                new ConstExp.NumberNode( ErrorHandler.NO_LOCATION,
                        currentScope, it, 7) );
        ist.resolveType(noLoc);
        bst = new Type.SubrangeType(  
                new ConstExp.NumberNode( ErrorHandler.NO_LOCATION, 
                        currentScope, bt, 0),
                new ConstExp.NumberNode( ErrorHandler.NO_LOCATION,
                        currentScope, bt, 1) );
        bst.resolveType(noLoc);
        isst = new Type.SubrangeType(  
                new ConstExp.NumberNode( ErrorHandler.NO_LOCATION, 
                        currentScope, ist, 5),
                new ConstExp.NumberNode( ErrorHandler.NO_LOCATION,
                        currentScope, ist, 7) );
        isst.resolveType(noLoc);
        addop = Operator.ADD_OP;
        eqop = Operator.EQUALS_OP;
        
        rit = new Type.ReferenceType( it );
        iit = new Type.ProductType( it, it );
        iit.resolveType(noLoc);
        bbt = new Type.ProductType( bt, bt );
        bbt.resolveType(noLoc);
        iiit = new Type.FunctionType( iit, it );
        iibt = new Type.FunctionType( iit, bt );
        bbbt = new Type.FunctionType( bbt, bt );
        
        ix = new ExpNode.ConstNode(null, it, 42 );
        ix.setType( it );
        iv = new SymEntry.VarEntry( "iv", null, rit );
        iv.setScope(currentScope);
        ivx = new ExpNode.VariableNode(null, iv );
//      ivx.setType( it );
        rix = new ExpNode.DereferenceNode( it, ivx );
        isx = new ExpNode.NarrowSubrangeNode(null, ist, ix );
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'pl0.symbol_table.Type.getSpace()'
     */
    public void testGetSpace() {
        assertEquals( 0, et.getSpace() );
        assertEquals( 1, it.getSpace() );
        assertEquals( 1, bt.getSpace() );
        assertEquals( 2, pt.getSpace() );
        assertEquals( 1, ist.getSpace() );
        assertEquals( 3, ist.getLower() );
        assertEquals( 7, ist.getUpper() );
        assertEquals( 1, bst.getSpace() );
        assertEquals( 0, bst.getLower() );
        assertEquals( 1, bst.getUpper() );
        assertEquals( 1, rit.getSpace() );
        assertEquals( 2, iit.getSpace() );
        assertEquals( 2, bbt.getSpace() );
    }

    /*
     * Test method for 'pl0.symbol_table.Type.coerce()'
     */
    public void testCoerce() throws IncompatibleTypes {
        ExpNode result = it.coerceToType( ix );
        assertTrue( "int compatible with int",
                result == ix );
        result = it.coerceToType( ivx );
        assertTrue( "int variable coerces to dereference",
                result instanceof ExpNode.DereferenceNode &&
                ((ExpNode.DereferenceNode)result).getLeftValue() == ivx);
        result = ist.coerceToType( ix );
        assertTrue( "int coerces to subrange of int",
                result instanceof ExpNode.NarrowSubrangeNode &&
                ((ExpNode.NarrowSubrangeNode)result).getExp() == ix );
        result = it.coerceToType( isx );
        assertTrue( "int subrange coerces to int" + result,
                result instanceof ExpNode.WidenSubrangeNode && 
                ((ExpNode.WidenSubrangeNode)result).getExp() == isx );
    }   
    /*
     * Test method for 'pl0.symbol_table.Type.toString()'
     */
    public void testToString() {
        assertEquals( "int", it.toString() );
        assertEquals( "boolean", bt.toString() );
        assertEquals( "int[3..7]", ist.toString() );
        assertEquals( "boolean[0..1]", bst.toString() );
        assertEquals( "ref(int)", rit.toString() );
        assertEquals( "(int*int)", iit.toString() );
        assertEquals( "(boolean*boolean)", bbt.toString() );
        assertEquals( "((int*int)->int)", iiit.toString() );
        assertEquals( "((int*int)->boolean)", iibt.toString() );
        assertEquals( "((boolean*boolean)->boolean)", bbbt.toString() );
    }
}
