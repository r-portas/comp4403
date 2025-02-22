package tree;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Stack;

import machine.Operation;
import machine.StackMachine;
import source.Errors;
import syms.SymEntry;
import syms.Type;
import tree.StatementNode.*;

/** class CodeGenerator implements code generation using the
 * visitor pattern to traverse the abstract syntax tree.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $ 
 */
public class CodeGenerator implements DeclVisitor, StatementTransform<Code>,
                    ExpTransform<Code> {
    /** Current static level of nesting into procedures. */
    private int staticLevel;
    
    /** Table of code for each procedure */
    private Procedures procedures;
    
    /** Error message handler */
    private Errors errors;
    /** Track the tree node currently being checked (for debugging) */
    private Stack<String> nodeStack;

    // The maximum branch values
    private int maxBranchValue = 10000;
    private int minBranchValue = -10000;

    public CodeGenerator(Errors errors) {
        super();
        this.errors = errors;
        nodeStack = new Stack<String>();
        procedures = new Procedures();
    }

    /*-------------------- Main Method to start code generation --------*/

    /** Main generate code for this tree. */
    public Procedures generateCode( DeclNode.ProgramNode node ) {
        beginGen( "Program" );
        staticLevel = 1;        // Main program is at static level 1
        /* Generate the code for the main program and all procedures */
        visitProcedureNode( node );
        endGen( "Program" );
        return procedures;
    }
    
    /* -------------------- Visitor methods ----------------------------*/

    /** Generate code for a single procedure. */
    public void visitProcedureNode( DeclNode.ProcedureNode node ) {
        beginGen( "Procedure" );
        // Generate code for the block
        Code code = visitBlockNode( node.getBlock() );
        procedures.addProcedure( node.getProcEntry(), code );
        endGen( "Procedure" );
    }

    /** Generate code for a block. */
    public Code visitBlockNode( BlockNode node ) {
        beginGen( "Block" );
        /** Generate code to allocate space for local variables on
         * procedure entry.
         */
        Code code = new Code();
        code.genAllocStack( node.getBlockLocals().getVariableSpace() );
        /* Generate the code for the body */
        code.append( node.getBody().genCode( this ) );
        code.generateOp( Operation.RETURN );
        /** Generate code for local procedures. */
        /* Static level is one greater for the procedures. */
        staticLevel++;
        node.getProcedures().accept(this);
        staticLevel--;
        endGen( "Block" );
        return code;
    }

    /** Code generation for a declaration list */
    public void visitDeclListNode( DeclNode.DeclListNode node ) {
        beginGen( "DeclList" );
        for( DeclNode decl : node.getDeclarations() ) {
            decl.accept( this );
        }
        endGen( "DeclList" );
    }

    /*************************************************
     *  Statement node code generation visit methods
     *************************************************/
    /** Code generation for an erroneous statement should not be attempted. */
    public Code visitStatementErrorNode( StatementNode.ErrorNode node ) {
        errors.fatal( "PL0 Internal error: generateCode for Statement Error Node",
                node.getLocation() );
        return null;
    }

    /** Code generation for a skip */
    public Code visitSkipNode(StatementNode.SkipNode node) {
        beginGen("Skip");

        // Create an empty code object
        Code code = new Code();

        endGen("Skip");
        return code;
    }

    /**
     * Gets the size of the code for the case statement
     */
    private int getCaseCodeSize(StatementNode.CaseStatementNode node) {
        Code caseCode = new Code();

        for (Map.Entry<ConstExp, StatementNode> s : node.getCases().entrySet()) {

            caseCode.append(genCodeForCase(s.getValue(), 0));
        }

        return caseCode.size();

    }

    private Code genCodeForCase(StatementNode n, int jumpLoc) {
        Code code = new Code();

        code.append(n.genCode(this));
        code.genJumpAlways(jumpLoc);

        return code;
    }

    private int getDefaultCodeSize(StatementNode.CaseStatementNode node) {

        if (node.getDefaultCase() == null) {
            Code code = new Code();
            code.genLoadConstant(1);
            code.generateOp(Operation.STOP);
            return code.size();
        }

        return node.getDefaultCase().genCode(this).size();
    }

    private int getLabelMin(StatementNode.CaseStatementNode node) {
        Set<ConstExp> labels = node.getCases().keySet();
        int min = maxBranchValue;

        if (labels.size() > 0) {

            min = ((ConstExp) labels.toArray()[0]).getValue();

            for (ConstExp l : labels) {
                if (l.getValue() < min) {
                    min = l.getValue();
                }
            }
        }

        return min;
    }

    private int getLabelMax(StatementNode.CaseStatementNode node) {
        // Get min and max of tokens
        Set<ConstExp> labels = node.getCases().keySet();
        int max = minBranchValue;

        if (labels.size() > 0) {

            max = ((ConstExp) labels.toArray()[0]).getValue();

            for (ConstExp l : labels) {
                if (l.getValue() > max) {
                    max = l.getValue();
                }
            }
        }

        return max;
    }

    private int getSizeOfJmpInto(StatementNode.CaseStatementNode node) {
        // Parse the condition and push onto the stack
        ExpNode cond = node.getCondition();
        int jumpListEntrySize = 3;

        Code code = new Code();
        code.append(cond.genCode(this));

        // Subtract 1, so it jumps into the table correctly
        code.genLoadConstant(-1);
        code.generateOp(Operation.ADD);

        code.genLoadConstant(jumpListEntrySize);

        // Multipy the condition by the size to get where to jump to
        code.generateOp(Operation.MPY);

        // Branch to that location
        code.generateOp(Operation.BR);

        return code.size();
    }


    // TODO: Handle default case
    public Code visitCaseStatementNode(StatementNode.CaseStatementNode node) {
        beginGen( "CaseStatement" );

        Code code = new Code();

        ExpNode cond = node.getCondition();

        // code.append(cond.genCode(this));
        // code.generateOp(Operation.WRITE);
        
        // Push the size of each jump table entry
        // size = size(load_con) + size(br_always) = 2 + 1
        int jumpListEntrySize = 3;

        int max = getLabelMax(node);
        int min = getLabelMin(node);

        int totalSize = getCaseCodeSize(node);
        int totalJumpListSize;
        
        // If there are no cases
        if (min == maxBranchValue && max == minBranchValue) {
            totalJumpListSize = 0;
        } else {
            totalJumpListSize = jumpListEntrySize * (max - min + 1);
        }

        // Check if it is within the bounds of the jump table
        Code tempCode = new Code();
        tempCode.append(cond.genCode(this));
        tempCode.genLoadConstant(max);
        tempCode.generateOp(Operation.LESSEQ);
        // Jump all the code (excluding the default case)
        // TODO: Code offset is wrong
        tempCode.genJumpIfFalse(totalJumpListSize + totalSize + getSizeOfJmpInto(node));

        // Check the min
        code.genLoadConstant(min);
        code.append(cond.genCode(this));
        code.generateOp(Operation.LESSEQ);
        // Jump all the code (excluding the default case)
        code.genJumpIfFalse(totalJumpListSize + totalSize + tempCode.size() + getSizeOfJmpInto(node));

        // Append the tempCode
        // The order will be max check then min check
        code.append(tempCode);
        
        code.append(cond.genCode(this));

        // Subtract the min, so it jumps into the table correctly
        code.genLoadConstant(-min);
        code.generateOp(Operation.ADD);

        code.genLoadConstant(jumpListEntrySize);

        // Multipy the condition by the size to get where to jump to
        code.generateOp(Operation.MPY);
        // code.generateOp(Operation.WRITE);

        // Branch to that location
        code.generateOp(Operation.BR);


        // TODO: If default case, add size
        // because we want to jump right to the end
        int defaultCaseSize = getDefaultCodeSize(node);

        Code caseCode = new Code();
        HashMap<Integer, Integer> sizeLookup = new HashMap<Integer, Integer>();

        HashMap<ConstExp, StatementNode> cases = node.getCases();


        for (ConstExp n : node.getLabels()) {
            StatementNode sn = cases.get(n);

            sizeLookup.put(n.getValue(), caseCode.size());
            // Subtract 3 for the branch + loadcon
            Code statementCode = sn.genCode(this);
            // TODO: Check this
            int sizeToEnd = totalSize - (caseCode.size() + statementCode.size() + 3);

            // Generate the code
            caseCode.append(genCodeForCase(sn, sizeToEnd + defaultCaseSize));
        }

        // Generate the jump list
        for (int i = min; i <= max; i++) {
            // The size of the remaining code
            int jumpLength = jumpListEntrySize * (max - i);

            // and add the size of the other code
            // If its not valid, jump to the end, or default
            jumpLength += (sizeLookup.getOrDefault(i, totalSize));

            code.genJumpAlways(jumpLength); 
        }

        code.append(caseCode);
        

        if (node.getDefaultCase() == null) {
            code.genLoadConstant(1);
            code.generateOp(Operation.STOP);
        } else {
            code.append(node.getDefaultCase().genCode(this));
        }

        endGen( "CaseStatement" );
        return code;
    }

    public Code visitMultiAssignNode(StatementNode.MultiAssignNode node) {
        beginGen( "MultiAssign" );
        Code code = new Code();

        for (StatementNode.AssignmentNode n : node.getAssignments()) {
            code.append( n.getExp().genCode( this ) );
        }

        for (StatementNode.AssignmentNode n : node.getReverseAssignments()) {
            code.append( n.getVariable().genCode( this ) );
            code.genStore( (Type.ReferenceType)n.getVariable().getType() );
        }

        endGen( "MultiAssign" );
        return code;
    }
    
    /** Code generation for an assignment statement. */
    public Code visitAssignmentNode(StatementNode.AssignmentNode node) {
        beginGen( "Assignment" );
        /* Generate code to evaluate the expression */
        Code code = node.getExp().genCode( this );
        /* Generate the code to load the address of the variable */
        code.append( node.getVariable().genCode( this ) );
        /* Generate the store based on the type/size of value */
        code.genStore( (Type.ReferenceType)node.getVariable().getType() );
        endGen( "Assignment" );
        return code;
    }
    /** Generate code for a "write" statement. */
    public Code visitWriteNode( StatementNode.WriteNode node ) {
        beginGen( "Write" );
        Code code = node.getExp().genCode( this );
        code.generateOp( Operation.WRITE );
        endGen( "Write" );
        return code;
    }
    /** Generate code for a "call" statement. */
    public Code visitCallNode( StatementNode.CallNode node ) {
        beginGen( "Call" );
        SymEntry.ProcedureEntry proc = node.getEntry();
        Code code = new Code();
        /* Generate the call instruction. The second parameter is the
         * procedure's symbol table entry. The actual address is resolved 
         * at load time.
         */
        code.genCall( staticLevel - proc.getLevel(), proc );
        endGen( "Call" );
        return code;
    }
    /** Generate code for a statement list */
    public Code visitStatementListNode( StatementNode.ListNode node ) {
        beginGen( "StatementList" );
        Code code = new Code();
        for( StatementNode s : node.getStatements() ) {
            code.append( s.genCode( this ) );
        }
        endGen( "StatementList" );
        return code;
    }

    /** Generate code for an "if" statement. */
    public Code visitIfNode(StatementNode.IfNode node) {
        beginGen( "If" );
        /* Generate code to evaluate the condition and then and else parts */
        Code code = node.getCondition().genCode( this );
        Code thenCode = node.getThenStmt().genCode( this );
        Code elseCode = node.getElseStmt().genCode( this );
        /* Append a branch over then part code */
        code.genJumpIfFalse( thenCode.size() + Code.SIZE_JUMP_ALWAYS );
        /* Next append the code for the then part */
        code.append( thenCode );
        /* Append branch over the else part */
        code.genJumpAlways( elseCode.size() );
        /* Finally append the code for the else part */
        code.append( elseCode );
        endGen( "If" );
        return code;
    }
 
    /** Generate code for a "while" statement. */
    public Code visitWhileNode(StatementNode.WhileNode node) {
        beginGen( "While" );
        /* Generate the code to evaluate the condition. */
        Code code = node.getCondition().genCode( this );
        /* Generate the code for the loop body */
        Code bodyCode = node.getLoopStmt().genCode( this );
        /* Add a branch over the loop body on false.
         * The offset is the size of the loop body code plus 
         * the size of the branch to follow the body.
         */
        code.genJumpIfFalse( bodyCode.size() + Code.SIZE_JUMP_ALWAYS );
        /* Append the code for the body */
        code.append( bodyCode );
        /* Add a branch back to the condition.
         * The offset is the total size of the current code plus the
         * size of a Jump Always (being generated).
         */
        code.genJumpAlways( -(code.size() + Code.SIZE_JUMP_ALWAYS) );
        endGen( "While" );
        return code;
    }
    /*************************************************
     *  Expression node code generation visit methods
     *************************************************/
    /** Code generation for an erroneous expression should not be attempted. */
    public Code visitErrorExpNode( ExpNode.ErrorNode node ) { 
        errors.fatal( "PL0 Internal error: generateCode for ErrorExpNode",
                node.getLocation() );
        return null;
    }

    /** Generate code for a constant expression. */
    public Code visitConstNode( ExpNode.ConstNode node ) {
        beginGen( "Const" );
        Code code = new Code();
        if( node.getValue() == 0 ) {
            code.generateOp( Operation.ZERO );
        } else if( node.getValue() == 1 ) {
            code.generateOp( Operation.ONE );
        } else {
            code.genLoadConstant( node.getValue() );
        }
        endGen( "Const" );
        return code;
    }

    /** Generate code for a "read" expression. */
    public Code visitReadNode( ExpNode.ReadNode node ) {
        beginGen( "Read" );
        Code code = new Code();
        code.generateOp( Operation.READ );
        endGen( "Read" );
        return code;
    }
    
    /** Generate code for a operator expression. */
    public Code visitOperatorNode( ExpNode.OperatorNode node ) {
        beginGen( "Operator" );
        Code code;
        ExpNode args = node.getArg();
        switch ( node.getOp() ) {
        case ADD_OP:
            code = args.genCode( this );
            code.generateOp(Operation.ADD);
            break;
        case SUB_OP:
            code = args.genCode( this );
            code.generateOp(Operation.NEGATE);
            code.generateOp(Operation.ADD);
            break;
        case MUL_OP:
            code = args.genCode( this );
            code.generateOp(Operation.MPY);
            break;
        case DIV_OP:
            code = args.genCode( this );
            code.generateOp(Operation.DIV);
            break;
        case EQUALS_OP:
            code = args.genCode( this );
            code.generateOp(Operation.EQUAL);
            break;
        case LESS_OP:
            code = args.genCode( this );
            code.generateOp(Operation.LESS);
            break;
        case NEQUALS_OP:
            code = args.genCode( this );
            code.generateOp(Operation.EQUAL);
            code.genBoolNot();
            break;
        case LEQUALS_OP:
            code = args.genCode( this );
            code.generateOp(Operation.LESSEQ);
            break;
        case GREATER_OP:
            /* Generate argument values in reverse order and use LESS */
            code = genArgsInReverse( (ExpNode.ArgumentsNode)args );
            code.generateOp(Operation.LESS);
            break;
        case GEQUALS_OP:
            /* Generate argument values in reverse order and use LESSEQ */
            code = genArgsInReverse( (ExpNode.ArgumentsNode)args );
            code.generateOp(Operation.LESSEQ);
            break;
        case NEG_OP:
            code = args.genCode( this );
            code.generateOp(Operation.NEGATE);
            break;
        default:
            errors.fatal("PL0 Internal error: Unknown operator",
                    node.getLocation() );
            code = null;
        }
        endGen( "Operator" );
        return code;
    }

    /** Generate the code to load arguments (in order) */
    public Code visitArgumentsNode( ExpNode.ArgumentsNode node ) {
        beginGen( "Arguments" );
        Code code = new Code();
        for( ExpNode exp : node.getArgs() ) {
            code.append( exp.genCode( this ) );
        }
        endGen( "Arguments" );
        return code;
    }
    /** Generate operator operands in reverse order */
    private Code genArgsInReverse( ExpNode.ArgumentsNode args ) {
        beginGen( "ArgsInReverse" );
        List<ExpNode> argList = args.getArgs();
        Code code = new Code();
        for( int i = argList.size()-1; 0 <= i; i-- ) {
            code.append( argList.get(i).genCode( this ) );
        }
        endGen( "ArgsInReverse" );
        return code;
    }
    /** Generate code to dereference an RValue. */
    public Code visitDereferenceNode( ExpNode.DereferenceNode node ) {
        beginGen( "Dereference" );
        Code code = node.getLeftValue().genCode( this );
        code.genLoad( node.getType() );
        endGen( "Dereference" );
        return code;
    }
    /** Generate code for an identifier. */
    public Code visitIdentifierNode(ExpNode.IdentifierNode node) {
        /** Visit the corresponding constant or variable node. */
        errors.fatal("Internal error: code generator called on IdentifierNode",
                node.getLocation() );
        return null;
    }
    /** Generate code for a variable (Exp) reference. */
    public Code visitVariableNode( ExpNode.VariableNode node ) {
        beginGen( "Variable" );
        SymEntry.VarEntry var = node.getVariable();
        Code code = new Code();
        code.genMemRef( staticLevel - var.getLevel(), var.getOffset() );
        endGen( "Variable" );
        return code;
    }
    /** Generate code to perform a bounds check on a subrange. */
    public Code visitNarrowSubrangeNode(ExpNode.NarrowSubrangeNode node) {
        beginGen( "NarrowSubrange" );
        Code code = node.getExp().genCode( this );
        code.genBoundsCheck(node.getSubrangeType().getLower(), 
                node.getSubrangeType().getUpper());
        endGen( "NarrowSubrange" );
        return code;
    }

    /** Generate code to widen a subrange to an integer. */
    public Code visitWidenSubrangeNode(ExpNode.WidenSubrangeNode node) {
        beginGen( "WidenSubrange" );
        // Widening doesn't require anything extra
        Code code = node.getExp().genCode( this );
        endGen( "WidenSubrange" );
        return code;
    }

    /**************************** Support Methods ***************************/
    /** Push current node onto debug rule stack and increase debug level */
    private void beginGen( String node ) {
        nodeStack.push( node );
        errors.debugMessage("Generating " + node );
        errors.incDebug();
    }
    /** Pop current node from debug rule stack and decrease debug level */
    private void endGen( String node ) {
        errors.decDebug();
        errors.debugMessage("End generation of " + node );
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


}
