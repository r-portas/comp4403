package tree;

/** 
 * interface StatementVisitor - Provides the interface for the visitor pattern 
 * to be applied to an abstract syntax tree node for a statement. 
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 * A class implementing this interface (such as the code generator) must 
 * provide implementations for visit methods for each of the statement 
 * node type. 
 * For example, the visit methods provided by the code generator statement
 * visitor implement the code generation for each type of statement node. 
 */
public interface  StatementTransform<ResultType> {

    ResultType visitBlockNode( StatementNode.BlockNode node );
    
    ResultType visitStatementErrorNode( StatementNode.ErrorNode node );

    ResultType visitStatementListNode( StatementNode.ListNode node );

    ResultType visitAssignmentNode( StatementNode.AssignmentNode node);

    ResultType visitWriteNode( StatementNode.WriteNode node);

    ResultType visitCallNode( StatementNode.CallNode node);
    ResultType visitIfNode( StatementNode.IfNode node);

    ResultType visitWhileNode( StatementNode.WhileNode node );

    ResultType visitReturnNode( StatementNode.ReturnNode node );
}
