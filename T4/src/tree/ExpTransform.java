package tree;

/**
 * interface ExpTransform - Handles visitor pattern for transforming expressions.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 */
public interface ExpTransform<ResultType> {
    ResultType visitErrorExpNode(ExpNode.ErrorNode node);
    ResultType visitConstNode(ExpNode.ConstNode node);
    ResultType visitIdentifierNode(ExpNode.IdentifierNode node);
    ResultType visitVariableNode(ExpNode.VariableNode node);
    ResultType visitReadNode(ExpNode.ReadNode node);
    ResultType visitOperatorNode(ExpNode.OperatorNode node);
    ResultType visitArgumentsNode(ExpNode.ArgumentsNode node);
    ResultType visitDereferenceNode(ExpNode.DereferenceNode node);
    ResultType visitNarrowSubrangeNode(ExpNode.NarrowSubrangeNode node);
    ResultType visitWidenSubrangeNode(ExpNode.WidenSubrangeNode node);
}
