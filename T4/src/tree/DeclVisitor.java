package tree;


/** 
 * interface DeclVisitor - Visitor pattern for declarations and procvedures.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 * Provides the interface for the visitor pattern to be applied to an
 * abstract syntax tree. A class implementing this interface (such as the
 * static checker) must provide implementations for visit methods for
 * each of the tree node type. 
 * For example, the visit methods provided by the static checker tree
 * visitor implement the type checks for each type of tree node. 
 */
public interface DeclVisitor {

    void visitDeclListNode(DeclNode.DeclListNode node);

    void visitProcedureNode(DeclNode.ProcedureNode node);
}
