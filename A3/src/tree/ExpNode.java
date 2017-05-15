package tree;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java_cup.runtime.ComplexSymbolFactory.Location;
import syms.Predefined;
import syms.SymEntry;
import syms.Type;

/** 
 * class ExpNode - Abstract Syntax Tree representation of expressions.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 * Abstract class representing expressions.
 * Actual expression nodes extend ExpNode.
 * All expression nodes have a location and a type.
 */
public abstract class ExpNode {
    /** Location in the source code of the expression */
    protected Location loc;
    /** Type of the expression (determined by static checker) */
    protected Type type;
    
    /** Constructor when type is known */
    protected ExpNode( Location loc, Type type) {
        this.loc = loc;
        this.type = type;
    }
    /** Constructor when type as yet unknown */
    protected ExpNode( Location loc ) {
        this( loc, Type.ERROR_TYPE );
    }
    public Type getType() {
        return type;
    }
    public void setType( Type type ) {
        this.type = type;
    }
    public Location getLocation() {
        return loc;
    }
    
    /** Each subclass of ExpNode must provide a transform method
     * to do type checking and transform the expression node to 
     * handle type coercions, etc.
     * @param visitor object that implements a traversal.
     * @return transformed expression node
     */
    public abstract ExpNode transform( ExpTransform<ExpNode> visitor );

    /** Each subclass of ExpNode must provide a genCode method
     * to visit the expression node to handle code generation.
     * @param visitor object that implements a traversal.
     * @return generated code
     */
    public abstract Code genCode( ExpTransform<Code> visitor );
        
    /** Tree node representing an erroneous expression. */
    public static class ErrorNode extends ExpNode {
        
        public ErrorNode( Location loc ) {
            super( loc, Type.ERROR_TYPE );
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitErrorExpNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitErrorExpNode( this );
        }
        @Override
        public String toString() {
            return "ErrorNode";
        }
    }

    /** Tree node representing a return node */
    public static class ReturnExpNode extends ExpNode {
        private String id;
        private SymEntry.ProcedureEntry procEntry;
        private List<ExpNode.ActualParamNode> parameters;

        public ReturnExpNode( Location loc, String id ) {
            super( loc );
            this.id = id;

            this.parameters = new ArrayList<ExpNode.ActualParamNode>();
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitReturnExpNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitReturnExpNode( this );
        }

        /**
         * Sets the parameters of the call
         * @param parameters The list of parameters
         */
        public void setParameters(List<ExpNode.ActualParamNode> parameters) {
            this.parameters = parameters;
        }

        /**
         * Returns a list of parameters
         *
         * @return A list of parameters
         */
        public List<ExpNode.ActualParamNode> getParameters() {
            return this.parameters;
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
        public String toString() {
            String s = "RETURN " + id;
            return s + ")";
        }
    }

    /** Tree node representing an actual parameter */
    public static class ActualParamNode extends ExpNode { 
        // The condition of the parameter
        private ExpNode condition;

        // The identifier
        private String identifier;

        public ActualParamNode( Location loc, String identifier, ExpNode condition ) {
            super( loc );
           
            this.identifier = identifier;
            this.condition = condition;
        }

        /**
         * Returns the identifier
         *
         * @return String
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Returns the condition
         *
         * @return ExpNode
         */
        public ExpNode getCondition() {
            return condition;
        }

        /**
         * Sets the condition
         *
         * @param condition The condition to set to
         */
        public void setCondition(ExpNode condition) {
            this.condition = condition;
        }

        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitActualParamNode( this );
        }

        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitActualParamNode( this );
        }
    }

    /** Tree node representing a constant within an expression. */
    public static class ConstNode extends ExpNode {
        /** constant's value */
        private int value;

        public ConstNode( Location loc, Type type, int value ) {
            super( loc, type );
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitConstNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitConstNode( this );
        }
        @Override
        public String toString( ) {
            return Integer.toString(value);
        }
    }

    /** Identifier node is used until the identifier can be resolved 
     * to be either a constant or a variable during the static 
     * semantics check phase. 
     */
    public static class IdentifierNode extends ExpNode {
        /** Name of the identifier */
        private String id;
        
        public IdentifierNode( Location loc, String id ) {
            super( loc );
            this.id = id;
        }
        public String getId() {
            return id;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitIdentifierNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitIdentifierNode( this );
        }
        @Override
        public String toString() {
            return "IdentifierNode(" + id + ")";
        }
    }
    /** Tree node representing a variable. */
    public static class VariableNode extends ExpNode {
        /** Symbol table entry for the variable */
        protected SymEntry.VarEntry variable;
    
        public VariableNode( Location loc, SymEntry.VarEntry variable ) {
            super( loc, variable.getType() );
            this.variable = variable;
        }
        public SymEntry.VarEntry getVariable() {
            return variable;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitVariableNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitVariableNode( this );
        }
        @Override
        public String toString( ) {
            return variable.getIdent();
        }
    }
    /** Tree node representing a "read" expression. */
    public static class ReadNode extends ExpNode {

        public ReadNode( Location loc ) {
            super( loc, Predefined.INTEGER_TYPE );
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitReadNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitReadNode( this );
        }
        @Override
        public String toString( ) {
            return "Read";
        }
    }
    /** Tree node for an operator. */
    public static class OperatorNode extends ExpNode {
        /** Operator, e.g. binary or unary operator */
        private Operator op;
        /** Argument(s) for operator. If more than one argument then this is
         * an ArgumentsNode 
         */
        private ExpNode arg;
        
        public OperatorNode( Location loc, Operator op, ExpNode arg ) {
            super( loc );
            this.op = op;
            this.arg = arg;
        }
        public Operator getOp() {
            return op;
        }
        public ExpNode getArg() {
            return arg;
        }
        public void setArg( ExpNode arg ) {
            this.arg = arg;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitOperatorNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitOperatorNode( this );
        }
        @Override
        public String toString() {
            return op + "(" + arg + ")";
        }
    }

    /** Tree node for a list of arguments */
    public static class ArgumentsNode extends ExpNode {
        /** List of arguments */
        private List<ExpNode> args;
        
        /** @requires args is non-empty */
        public ArgumentsNode( Type.ProductType t, List<ExpNode> args ) {
            super( args.get(0).getLocation(), t );
            this.args = args;
        }
        /** @requires args is non-empty */
        public ArgumentsNode( List<ExpNode> args ) {
            super( args.get(0).getLocation() );
            this.args = args;
        }
        /** @requires exps is non-empty */
        public ArgumentsNode( ExpNode... exps ) {
            this( Arrays.asList( exps ) );
        }
        public List<ExpNode> getArgs() {
            return args;
        }
        public void setArgs( List<ExpNode> args ) {
            this.args = args;
        }
        @Override
        public ExpNode transform(ExpTransform<ExpNode> visitor ) {
            return visitor.visitArgumentsNode( this );
        }
        @Override
        public Code genCode(ExpTransform<Code> visitor ) {
            return visitor.visitArgumentsNode( this );
        }
        @Override
        public String toString() {
            return args.toString();
        }
    }

    /** Tree node for dereferencing an LValue.
     * A Dereference node references an ExpNode node and represents the
     * dereferencing of the "address" given by the leftValue to give
     * the value at that address.
     */
    public static class DereferenceNode extends ExpNode {
        /** LValue to be dereferenced */
        private ExpNode leftValue;

        public DereferenceNode( Type type, ExpNode exp ) {
            super( exp.getLocation(), type );
            this.leftValue = exp;
        }
        public ExpNode getLeftValue() {
            return leftValue;
        }
        public void setLeftValue( ExpNode leftValue ) {
            this.leftValue = leftValue;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitDereferenceNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitDereferenceNode( this );
        }
        @Override
        public String toString( ) {
            return "Dereference(" + leftValue + ")";
        }
    }

    /** Tree node representing a coercion that narrows a subrange */
    public static class NarrowSubrangeNode extends ExpNode {
        /** Expression to be narrowed */
        private ExpNode exp;

        /* @requires type instance of Type.SubrangeType &&
         *           exp.getType().equals( type.getBaseType() ) */
        public NarrowSubrangeNode( Location loc, Type.SubrangeType type, 
                ExpNode exp )
        {
            super( loc, type );
            assert type instanceof Type.SubrangeType &&
                   exp.getType().equals( type.getBaseType() );
            this.exp = exp;
        }
        public Type.SubrangeType getSubrangeType() {
            return (Type.SubrangeType)getType();
        }
        public ExpNode getExp() {
            return exp;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitNarrowSubrangeNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitNarrowSubrangeNode( this );
        }
        @Override
        public String toString() {
            return "NarrowSubrange(" + exp + ":" + getType() + ")";
        }
    }
    
    /** Tree node representing a widening of a subrange */
    public static class WidenSubrangeNode extends ExpNode {
        /** Expression to be widened */
        private ExpNode exp;

        /* @requires exp.getType() instanceof Type.SubrangeType &&
         *           exp.getType().getBaseType().equals( type ) */
        public WidenSubrangeNode( Location loc, Type type, ExpNode exp ) {
            super( loc, type );
            assert exp.getType() instanceof Type.SubrangeType &&
                   ((Type.SubrangeType)exp.getType()).getBaseType().equals( type );
            this.exp = exp;
        }
        public ExpNode getExp() {
            return exp;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitWidenSubrangeNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitWidenSubrangeNode( this );
        }
        @Override
        public String toString() {
            return "WidenSubrange(" + exp + ":" + getType() + ")";
        }
    }
}
