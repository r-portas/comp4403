package tree;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import source.Errors;
import java_cup.runtime.ComplexSymbolFactory.Location;
import syms.Predefined;
import syms.SymEntry;
import syms.Scope;
import syms.SymbolTable;
import syms.Type;
import syms.Type.IncompatibleTypes;
import tree.DeclNode.DeclListNode;
import tree.StatementNode.*;

/** class StaticSemantics - Performs the static semantic checks on
 * the abstract syntax tree using a visitor pattern to traverse the tree.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 * See the notes on the static semantics of PL0 to understand the PL0
 * type system in detail.
 */
public class StaticChecker implements DeclVisitor, StatementVisitor, 
                                        ExpTransform<ExpNode> {

    /** The static checker maintains a symbol table reference.
     * Its current scope is that for the procedure 
     * currently being processed.
     */
    private SymbolTable symtab;
    /** Errors are reported through the error handler. */
    private Errors errors;
    /** Track the tree node currently being checked (for debugging) */
    private Stack<String> nodeStack;

    /** Construct a static checker for PL0.
     * @param errors is the error message handler.
     */
    public StaticChecker( Errors errors ) {
        super();
        this.errors = errors;
        nodeStack = new Stack<String>();
    }

    public ExpNode visitRecordNode(ExpNode.RecordNode node) {
        beginCheck( "Record" );

        Type type = node.getType().resolveType( node.getLocation() );
        node.setType(type);

        Type.RecordType recType = type.getRecordType();
        if (recType == null) {
            staticError("Type is not a record type", node.getLocation());
        }

        ArrayList<String> fieldNames = new ArrayList<String>();
        // Check for duplicates
        for (Type.Field field : recType.getFieldList()) {
            String fieldName = field.getId();

            if (fieldNames.contains(fieldName)) {
                staticError("Record declaration contains duplicate identifiers", field.getLocation());
            }

            fieldNames.add(fieldName);
        }

        // Transform the expressions
        for (int i = 0; i < node.getRecordFields().size(); i++) {
            ExpNode exp  = node.getRecordFields().get(i);
            node.getRecordFields().set(i, exp.transform( this ));
        }

        // Check the number of elements
        int numOfElements = node.getRecordFields().size();
        int numOfFields = recType.getFieldList().size();

        if (numOfElements != numOfFields) {
            staticError("Too few expressions for fields in record", node.getLocation());
        }

        endCheck( "Record" );
        return node;
    }

    public ExpNode visitPointerNode(ExpNode.PointerNode node) {
        beginCheck( "Pointer" );

        // Resolve the type
        Type type = node.getPointerType().resolveType( node.getLocation() );
        node.setType(type);

        SymEntry.TypeEntry symType = symtab.getCurrentScope().lookupType(type.getName());
        if (symType == null) {
            staticError("Undefined type: " + symType, node.getLocation());
        }
        
        endCheck( "Pointer" );
        return node;
    }

    public ExpNode visitDerefPointerNode(ExpNode.DerefPointerNode node) {
        beginCheck( "Deref Pointer Node" );

        ExpNode pointer = node.getPointer().transform( this );
        node.setPointer(pointer);

        Type.PointerType pointerType = pointer.getType().getPointerType();

        if (pointerType == null) {
            staticError("type must be a pointer", node.getLocation());
            node.setType(Type.ERROR_TYPE);
            endCheck( "Deref Pointer Node" );
            return node;
        }


        Type baseType = pointerType.getBaseType();

        // Create a reference to the base type
        Type.ReferenceType ref = new Type.ReferenceType(baseType);
        // Update the node's type with the new reference
        node.setType(ref);

        endCheck( "Deref Pointer Node" );

        return node;
    }

    public ExpNode visitRecordReferenceNode(ExpNode.RecordReferenceNode node) {
        beginCheck( "Record Reference" );

        ExpNode record = node.getRecord().transform( this );

        // Convert to a record type
        Type.RecordType recType = record.getType().getRecordType();

        if (recType == null) {
            // This is a runtime thing
            staticError("must be a record type, found " + record.getType(), node.getLocation());
            node.setType(Type.ERROR_TYPE);
            endCheck( "Record Reference" );
            return node;
        }

        // Update the type
        record.setType(recType);
        node.setRecord(record);

        boolean foundMatch = false;

        for (Type.Field f : recType.getFieldList()) {
            String id = f.getId();
            if (node.getIdentifier().equals(id)) {
                if (foundMatch == false) {
                    foundMatch = true;
                } else {
                    // foundMatch is true, which means we have a duplicate
                    // staticError("Record has duplicates", node.getLocation());
                    // node.setType(Type.ERROR_TYPE);
                }
            }
        }

        if (foundMatch == false) {
            staticError("record doesn't contain attribute " + node.getIdentifier(), node.getLocation());
            node.setType(Type.ERROR_TYPE);
        }

        // Update the type of the node
        node.setType(recType.getFieldType(node.getIdentifier()));

        if (node.getType() != Type.ERROR_TYPE) {
            Type refType = new Type.ReferenceType(node.getType());
            node.setType(refType);
        }

        endCheck( "Record Reference" );
        return node;
    }


    /** The tree traversal starts with a call to visitProgramNode.
     * Then its descendants are visited using visit methods for each
     * node type, which are called using the visitor pattern "accept"
     * method (or "transform" for expression nodes) of the abstract
     * syntax tree node.
     */
    public void visitProgramNode(DeclNode.ProgramNode node) {
        beginCheck( "Program" );
        // Set up the symbol table to be that for the main program.
        symtab = node.getBaseSymbolTable();
        // The main program is a special case of a procedure
        visitProcedureNode( node );
        endCheck( "Program" );
    }
    /** Procedure, function or main program node */
    public void visitProcedureNode(DeclNode.ProcedureNode node) {
        beginCheck("Procedure");
        SymEntry.ProcedureEntry procEntry = node.getProcEntry();
        // Set the current symbol table scope to that for the procedure.
        symtab.reenterScope( procEntry.getLocalScope() );
        // resolve all references to identifiers with the declarations
        symtab.getCurrentScope().resolveScope();
        // Check the block of the procedure.
        visitBlockNode( node.getBlock() );
        endCheck("Procedure");
    }
    public void visitBlockNode(BlockNode node) {
        beginCheck("Block");
        // Check the procedures, if any.
        node.getProcedures().accept( this );
        // Check the body of the block.
        node.getBody().accept( this );
        // System.out.println( symtab );
        // Restore the symbol table to the parent scope (not really necessary)
        symtab.leaveScope();
        //System.out.println( "Block Body\n" + node.getBody() );
        endCheck("Block");
    }
    public void visitDeclListNode(DeclListNode node) {
        beginCheck("DeclList");
        for( DeclNode declaration : node.getDeclarations() ) {
            declaration.accept( this );
        }
        endCheck("DeclList");
    }
    /*************************************************
     *  Statement node static checker visit methods
     *************************************************/
    public void visitStatementErrorNode(StatementNode.ErrorNode node) {
        beginCheck("StatementError");
        // Nothing to check - already invalid.
        endCheck("StatementError");
    }

    public void visitAssignmentNode(StatementNode.AssignmentNode node) {
        beginCheck("Assignment");
        // Check the left side left value.
        ExpNode left = node.getVariable().transform( this );
        node.setVariable( left );

        // Check the right side expression.
        ExpNode exp = node.getExp().transform( this );
        node.setExp( exp );
        // Validate that it is a true left value and not a constant.
        Type lvalType = left.getType();
        if( ! (lvalType instanceof Type.ReferenceType) ) {
            if( lvalType != Type.ERROR_TYPE ) {
                staticError( "variable expected, type = " + lvalType , 
                        left.getLocation() );
            }
        } else {
            /* Validate that the right expression is assignment
             * compatible with the left value. This may require that the 
             * right side expression is coerced to the dereferenced
             * type of the left side LValue. */
            Type baseType = ((Type.ReferenceType)lvalType).getBaseType();

            node.setExp( baseType.coerceExp( exp ) );
        }
        endCheck("Assignment");
    }

    public void visitWriteNode(StatementNode.WriteNode node) {
        beginCheck("Write");
        // Check the expression being written.
        ExpNode exp = node.getExp().transform( this );
        // coerce expression to be of type integer,
        // or complain if not possible.
        node.setExp( Predefined.INTEGER_TYPE.coerceExp( exp ) );
        endCheck("Write");
    }

    
    public void visitCallNode(StatementNode.CallNode node) {
        beginCheck("Call");
        SymEntry.ProcedureEntry procEntry;
        // Look up the symbol table entry for the procedure.
        SymEntry entry = symtab.getCurrentScope().lookup( node.getId() );
        if( entry instanceof SymEntry.ProcedureEntry ) {
            procEntry = (SymEntry.ProcedureEntry)entry;
            node.setEntry( procEntry );
        } else {
            staticError( "Procedure identifier required", node.getLocation() );
            endCheck("Call");
            return;
        }
        endCheck("Call");
    }

    public void visitStatementListNode( StatementNode.ListNode node ) {
        beginCheck("StatementList");
        for( StatementNode s : node.getStatements() ) {
            s.accept( this );
        }
        endCheck("StatementList");
    }
    private ExpNode checkCondition( ExpNode cond ) {
        // Check and transform the condition
        cond = cond.transform( this );
        /* Validate that the condition is boolean, which may require
         * coercing the condition to be of type boolean. */     
        return Predefined.BOOLEAN_TYPE.coerceExp( cond );
    }
    public void visitIfNode(StatementNode.IfNode node) {
        beginCheck("If");
        // Check the condition.
        node.setCondition( checkCondition( node.getCondition() ) );
        // Check the 'then' part.
        node.getThenStmt().accept( this );
        // Check the 'else' part.
        node.getElseStmt().accept( this );
        endCheck("If");
    }

    public void visitWhileNode(StatementNode.WhileNode node) {
        beginCheck("While");
        // Check the condition.
        node.setCondition( checkCondition( node.getCondition() ) );
        // Check the body of the loop.
        node.getLoopStmt().accept( this );
        endCheck("While");
    }
    /*************************************************
     *  Expression node static checker visit methods.
     *  The static checking visitor methods for expressions
     *  transform the expression to include resolved identifier
     *  nodes, and add nodes like dereference nodes, and
     *  narrow and widen subrange nodes.
     *  These ensure that the transformed tree is type consistent.
     *************************************************/
    public ExpNode visitErrorExpNode(ExpNode.ErrorNode node) {
        beginCheck("ErrorExp");
        // Nothing to do - already invalid.
        endCheck("ErrorExp");
        return node;
    }

    public ExpNode visitConstNode(ExpNode.ConstNode node) {
        beginCheck("Const");
        // type already set up
        endCheck("Const");
        return node;
    }
    /** Reads an integer value from input */
    public ExpNode visitReadNode(ExpNode.ReadNode node) {
        beginCheck("Read");
        // type already set up
        endCheck("Read");
        return node;
    }
    /** Handles binary and unary operators, 
     * allowing the types of operators to be overloaded.
     */
    public ExpNode visitOperatorNode( ExpNode.OperatorNode node ) {
        beginCheck( "Operator" );
        /* Operators can be overloaded */
        /* Check the arguments to the operator */
        ExpNode arg = node.getArg().transform( this );
        /* Lookup the operator in the symbol table to get its type */
        Type opType = symtab.getCurrentScope().
                lookupOperator( node.getOp().getName() ).getType();
        
        if( opType instanceof Type.FunctionType ) {
            /* The operator is not overloaded. Its type is represented
             * by a FunctionType from its argument's type to its
             * result type.
             */
            Type.FunctionType fType = (Type.FunctionType)opType;
            node.setArg( fType.getArgType().coerceExp( arg ) );
            node.setType( fType.getResultType() );
        } else if( opType instanceof Type.IntersectionType ) {
            /* The operator is overloaded. Its type is represented
             * by an IntersectionType containing a set of possible
             * types for the operator, each of which is a FunctionType.
             * Each possible type is tried until one succeeds.
             */
            errors.debugMessage("Coercing " + arg + " to " + opType);
            errors.incDebug();

            for( Type t : ((Type.IntersectionType)opType).getTypes() ) {
                Type.FunctionType fType = (Type.FunctionType)t;
                Type opArgType = fType.getArgType();
                try {
                    /* Coerce the argument to the argument type for 
                     * this operator type. If the coercion fails an
                     * exception will be trapped and an alternative 
                     * function type within the intersection tried.
                     */
                    ExpNode newArg = opArgType.coerceToType( arg );
                    /* The coercion succeeded if we get here */
                    node.setArg( newArg );
                    node.setType( fType.getResultType() );
                    errors.decDebug();
                    endCheck( "Operator" );
                    return node;
                } catch ( IncompatibleTypes ex ) {
                    // Allow "for" loop to try an alternative
                }
            }
            errors.decDebug();
            errors.debugMessage("Failed to coerce " + arg + " to " + opType);
            // no match in intersection type
            staticError( "Type of argument " + arg.getType().getName() + 
                    " does not match " + opType.getName(), node.getLocation() );
            node.setType( Type.ERROR_TYPE );
        } else {
            errors.fatal( "Invalid operator type", node.getLocation() );
        }
        endCheck( "Operator" );
        return node;
    }
    /** An ArgumentsNode is used to represent a list of arguments, each 
     * of which is an expression. The arguments for a binary operator are 
     * represented by list with two elements.
     */
    public ExpNode visitArgumentsNode( ExpNode.ArgumentsNode node ) {
        beginCheck("Arguments");
        List<ExpNode> newExps = new LinkedList<ExpNode>();
        List<Type> types = new LinkedList<Type>();
        for( ExpNode exp : node.getArgs() ) {
            ExpNode newExp = exp.transform( this );
            
            if (newExp.getType().getPointerType() != null) {
            	// If its a pointer, we want to use it instead
            	newExp.setType(newExp.getType().getPointerType());
            	Type.PointerType pType = (Type.PointerType)newExp.getType();
            	types.add(pType);
            } else {
            	types.add( newExp.getType() );
            }
            newExps.add( newExp );
            
        }
        node.setArgs( newExps );
        node.setType( new Type.ProductType( types ) );
        endCheck("Arguments");
        return node;
    }
    /** A DereferenceNode allows a variable (of type ref(int) say) to be
     * dereferenced to get its value (of type int). 
     */
    public ExpNode visitDereferenceNode(ExpNode.DereferenceNode node) {
        beginCheck("Dereference");
        // Check the left value referred to by this dereference node
        ExpNode lVal = node.getLeftValue().transform( this );
        node.setLeftValue( lVal );
        /* The type of the dereference node is the base type of its 
         * left value. */
        Type lValueType = lVal.getType();
        if( lValueType instanceof Type.ReferenceType ) {
            node.setType( lValueType.optDereferenceType() ); // not optional here
        } else if( lValueType != Type.ERROR_TYPE ) { // avoid cascading errors
            staticError( "cannot dereference an expression which isn't a reference",
                    node.getLocation() );
        }
        endCheck("Dereference");
        return node;
    }
    /** When parsing an identifier within an expression one can't tell
     * whether it has been declared as a constant or an identifier.
     * Here we check which it is and return either a constant or 
     * a variable node.
     */
    public ExpNode visitIdentifierNode(ExpNode.IdentifierNode node) {
        beginCheck("Identifier");
        // First we look up the identifier in the symbol table.
        ExpNode newNode;
        SymEntry entry = symtab.getCurrentScope().lookup( node.getId() );

        if( entry instanceof SymEntry.ConstantEntry ) {
            // Set up a new node which is a constant.
            debugMessage("Transformed " + node.getId() + " to Constant");
            SymEntry.ConstantEntry constEntry = 
                (SymEntry.ConstantEntry)entry;
            newNode = new ExpNode.ConstNode( node.getLocation(), 
                    constEntry.getType(), constEntry.getValue() );
        } else if( entry instanceof SymEntry.VarEntry ) {
            debugMessage("Transformed " + node.getId() + " to Variable");
            // Set up a new node which is a variable.
            SymEntry.VarEntry varEntry = (SymEntry.VarEntry)entry;
            newNode = new ExpNode.VariableNode(node.getLocation(), varEntry);
        } else {
            // Undefined identifier or a type or procedure identifier.
            // Set up new node to be an error node.
            newNode = new ExpNode.ErrorNode( node.getLocation() );
            //System.out.println( "Entry = " + entry );
            staticError("Constant or variable identifier required", node.getLocation() );
        }

        endCheck("Identifier");
        return newNode;
    }

    public ExpNode visitVariableNode(ExpNode.VariableNode node) {
        beginCheck("Variable");
        // Type already set up
        endCheck("Variable");
        return node;
    }
    public ExpNode visitNarrowSubrangeNode(ExpNode.NarrowSubrangeNode node) {
        beginCheck("NarrowSubrange");
        // Nothing to do.
        endCheck("NarrowSubrange");
        return node;
    }

    public ExpNode visitWidenSubrangeNode(ExpNode.WidenSubrangeNode node) {
        beginCheck("WidenSubrange");
        // Nothing to do.
        endCheck("WidenSubrange");
        return node;
    }

    /**************************** Support Methods ***************************/
    /** Push current node onto debug rule stack and increase debug level */
    private void beginCheck( String node ) {
        nodeStack.push( node );
        errors.debugMessage("Checking " + node );
        errors.incDebug();
    }
    /** Pop current node from debug rule stack and decrease debug level */
    private void endCheck( String node ) {
        errors.decDebug();
        errors.debugMessage("End check of " + node );
        String popped = nodeStack.pop();
        if( node != popped) {
            errors.debugMessage("*** End node " + node + 
                    " does not match start node " + popped);
        }
    }
    /** Debugging message output */
    private void debugMessage( String msg ) {
        errors.debugMessage( msg );
    }
    /** Error message handle for parsing errors */
    private void staticError( String msg, Location loc ) {
        errors.debugMessage( msg );
        errors.error( msg, loc );
    }
}
