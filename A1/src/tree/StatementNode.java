package tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import syms.Scope;
import java_cup.runtime.ComplexSymbolFactory.Location;
import syms.SymEntry;

/** 
 * class StatementNode - Abstract syntax tree representation of statements. 
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 * Classes defined within StatementNode extend it.
 */
public abstract class StatementNode {
    /** Location in the input source program (line and column number effectively).
     * All statements have a location within the original source code.
     */
    private Location loc;

    /** Constructor */
    protected StatementNode( Location loc ) {
        this.loc = loc;
    }
    public Location getLocation() {
        return loc;
    }
    
    /** All statement nodes provide an accept method to implement the visitor
     * pattern to traverse the tree.
     * @param visitor class implementing the details of the particular
     *  traversal.
     */
    public abstract void accept( StatementVisitor visitor );
    
    /** All statement nodes provide a genCode method to implement the visitor
     * pattern to traverse the tree for code generation.
     * @param visitor class implementing the code generation
     */
    public abstract Code genCode( StatementTransform<Code> visitor );
    
    /** Debugging output of a statement at an indent level */
    public abstract String toString( int level );
    
    /** Debugging output at level 0 */
    @Override
    public String toString() {
        return this.toString(0);
    }
    
    /** Returns a string with a newline followed by spaces of length 2n. */
    public static String newLine( int n ) {
       String indent = "\n";
       while( n > 0) {
           indent += "  ";
           n--;
       }
       return indent;
    }
    
    /** Node representing a Block consisting of declarations and
     * body of a procedure, function, or the main program. */
    public static class BlockNode extends StatementNode {
        protected DeclNode.DeclListNode procedures;
        protected StatementNode body;
        protected Scope blockLocals;

        /** Constructor for a block within a procedure */
        public BlockNode( Location loc, DeclNode.DeclListNode procedures, 
                StatementNode body) {
            super( loc );
            this.procedures = procedures;
            this.body = body;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitBlockNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitBlockNode( this );
        }

        public DeclNode.DeclListNode getProcedures() {
            return procedures;
        }
        public StatementNode getBody() {
            return body;
        }
        public Scope getBlockLocals() {
            return blockLocals;
        }
        public void setBlockLocals( Scope blockLocals ) {
            this.blockLocals = blockLocals;
        }
        @Override
        public String toString( int level ) {
            return getProcedures().toString(level+1) + 
                    newLine(level) + "BEGIN" + 
                    newLine(level+1) + body.toString(level+1) +
                    newLine(level) + "END";
        }
    }

    /** Statement node representing an erroneous statement. */
    public static class ErrorNode extends StatementNode {
        public ErrorNode( Location loc ) {
            super( loc );
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitStatementErrorNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitStatementErrorNode( this );
        }
        @Override
        public String toString( int level) {
            return "ERROR";
        }
    }

    public static class CaseStatementNode extends StatementNode {


        private HashMap<ConstExp, StatementNode> cases;
        private StatementNode defaultCase;

        public CaseStatementNode( Location loc, HashMap<ConstExp, StatementNode> cases, StatementNode defaultCase ) {
            super( loc );

            this.cases = cases;
            this.defaultCase = defaultCase;
        }

        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitCaseStatementNode( this );
        }

        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitCaseStatementNode( this );
        }

        public StatementNode getDefaultCase() {
            return defaultCase;
        }

        public HashMap<ConstExp, StatementNode> getCases() {
            return cases;
        }

        @Override
        public String toString( int level ) {
            return "case";
        }

    }

    public static class SkipNode extends StatementNode {
        public SkipNode( Location loc ) {
            super( loc );
        }

        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitSkipNode( this );
        }

        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitSkipNode( this );
        }

        @Override
        public String toString( int level ) {
            return "skip";
        }

    }

    /** Tree node representing an SingleAssign statement. */
    public static class SingleAssignNode extends StatementNode {
        /** Tree node for expression on left hand side of an assignment. */
        private ExpNode lValue;
        /** Tree node for the expression to be assigned. */
        private ExpNode exp;

        public SingleAssignNode( Location loc, ExpNode variable, ExpNode exp ) {
            super( loc );
            this.lValue = variable;
            this.exp = exp;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitSingleAssignNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitSingleAssignNode( this );
        }
        public ExpNode getVariable() {
            return lValue;
        }
        public void setVariable( ExpNode variable ) {
            this.lValue = variable;
        }
        public ExpNode getExp() {
            return exp;
        }
        public void setExp(ExpNode exp) {
            this.exp = exp;
        }
        public String getVariableName() {
            if( lValue instanceof ExpNode.VariableNode ) {
                return 
                    ((ExpNode.VariableNode)lValue).getVariable().getIdent();
            } else {
                return "<noname>";
            }
        }
        @Override
        public String toString( int level ) {
            return lValue.toString() + " := " + exp.toString();
        }
    }

    /** Tree node representing an assignment statement. */
    public static class AssignmentNode extends StatementNode {

        private List<SingleAssignNode> assignments;

        public AssignmentNode( Location loc, List<SingleAssignNode> assignments ) {
            super( loc );
            this.assignments = assignments;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitAssignmentNode( this );
        }

        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitAssignmentNode( this );
        }

        public List<SingleAssignNode> getAssignments() {
            return this.assignments;
        }

        @Override
        public String toString( int level ) {
            return "Assignment Node (" + this.assignments.toString() + ")";
        }
    }

    /** Tree node representing a "write" statement. */
    public static class WriteNode extends StatementNode {
        private ExpNode exp;

        public WriteNode( Location loc, ExpNode exp ) {
            super( loc );
            this.exp = exp;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitWriteNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitWriteNode( this );
        }
        public ExpNode getExp() {
            return exp;
        }
        public void setExp( ExpNode exp ) {
            this.exp = exp;
        }
        @Override
        public String toString( int level ) {
            return "WRITE " + exp.toString();
        }
    }
    
    /** Tree node representing a "call" statement. */
    public static class CallNode extends StatementNode {
        private String id;
        private SymEntry.ProcedureEntry procEntry;

        public CallNode( Location loc, String id ) {
            super( loc );
            this.id = id;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitCallNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitCallNode( this );
        }
        public String getId() {
            return id;
        }
        public SymEntry.ProcedureEntry getEntry() {
            return procEntry;
        }
        public void setEntry(SymEntry.ProcedureEntry entry) {
            this.procEntry = entry;
        }
        @Override
        public String toString( int level ) {
            String s = "CALL " + procEntry.getIdent() + "(";
            return s + ")";
        }
    }
    /** Tree node representing a statement list. */
    public static class ListNode extends StatementNode {
        private List<StatementNode> statements;
        
        public ListNode( Location loc ) {
            super( loc );
            this.statements = new ArrayList<StatementNode>();
        }
        public void addStatement( StatementNode s ) {
            statements.add( s );
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitStatementListNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitStatementListNode( this );
        }
        public List<StatementNode> getStatements() {
            return statements;
        }
        @Override
        public String toString( int level) {
            String result = "";
            String sep = "";
            for( StatementNode s : statements ) {
                result += sep + s.toString( level );
                sep = ";" + newLine(level);
            }
            return result;
        }
    }
    /** Tree node representing an "if" statement. */
    public static class IfNode extends StatementNode {
        private ExpNode condition;
        private StatementNode thenStmt;
        private StatementNode elseStmt;

        public IfNode( Location loc, ExpNode condition, 
                StatementNode thenStmt, StatementNode elseStmt ) {
            super( loc );
            this.condition = condition;
            this.thenStmt = thenStmt;
            this.elseStmt = elseStmt;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitIfNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitIfNode( this );
        }
        public ExpNode getCondition() {
            return condition;
        }
        public void setCondition( ExpNode cond ) {
            this.condition = cond;
        }
        public StatementNode getThenStmt() {
            return thenStmt;
        }
        public StatementNode getElseStmt() {
            return elseStmt;
        }
        @Override
        public String toString( int level ) {
            return "IF " + condition.toString() + " THEN" + 
                        newLine(level+1) + thenStmt.toString( level+1 ) + 
                    newLine( level ) + "ELSE" + 
                        newLine(level+1) + elseStmt.toString( level+1 );
        }
    }

    /** Tree node representing a "while" statement. */
    public static class WhileNode extends StatementNode {
        private ExpNode condition;
        private StatementNode loopStmt;

        public WhileNode( Location loc, ExpNode condition, 
              StatementNode loopStmt ) {
            super( loc );
            this.condition = condition;
            this.loopStmt = loopStmt;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitWhileNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitWhileNode( this );
        }
        public ExpNode getCondition() {
            return condition;
        }
        public void setCondition( ExpNode cond ) {
            this.condition = cond;
        }
        public StatementNode getLoopStmt() {
            return loopStmt;
        }
        @Override
        public String toString( int level ) {
            return "WHILE " + condition.toString() + " DO" +
                newLine(level+1) + loopStmt.toString( level+1 );
        }
    }
}

