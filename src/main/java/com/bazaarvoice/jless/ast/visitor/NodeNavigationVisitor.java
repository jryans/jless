package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.ArgumentsNode;
import com.bazaarvoice.jless.ast.node.ExpressionGroupNode;
import com.bazaarvoice.jless.ast.node.ExpressionNode;
import com.bazaarvoice.jless.ast.node.ExpressionPhraseNode;
import com.bazaarvoice.jless.ast.node.FilterArgumentNode;
import com.bazaarvoice.jless.ast.node.FunctionNode;
import com.bazaarvoice.jless.ast.node.LineBreakNode;
import com.bazaarvoice.jless.ast.node.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.PropertyNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorGroupNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.node.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.node.SimpleNode;
import com.bazaarvoice.jless.ast.node.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.node.VariableDefinitionNode;
import com.bazaarvoice.jless.ast.node.VariableReferenceNode;

/**
 * Visitors that need to {@link com.bazaarvoice.jless.ast.node.Node#traverse} or 
 * {@link com.bazaarvoice.jless.ast.node.Node#filter} a set of nodes in the tree
 * implement this interface.
 */
public interface NodeNavigationVisitor {

    // Base visitor methods
    // These eliminate the need for methods that no visitor uses

    /**
     * Called when the visitor first reaches a node with children.
     * @return Determines whether the node's children will be visited.
     */
    boolean enter(Node node);

    /**
     * Called when the visitor reaches a leaf node.
     * @return Determines whether the sibling nodes following this node will be visited.
     */
    boolean visit(Node node);

    /**
     * Called when the visitor is about to leave a node with children. This is called
     * after both {@link #enter} and optionally visiting the node's children, when permitted.
     * @return Determines whether the sibling nodes following this node will be visited.
     */
    boolean exit(Node node);

    /**
     * Called when the visitor reaches an invisible node.
     * @return Determines whether the navigation applied to visible nodes should also apply here.
     */
    boolean visitInvisible(Node node);

    // Concrete visitor methods

    boolean enter(ArgumentsNode node);

    boolean exit(ArgumentsNode node);

    boolean enter(ExpressionGroupNode node);

    boolean exit(ExpressionGroupNode node);

    boolean exit(ExpressionNode node);

    boolean exit(ExpressionPhraseNode node);

    boolean enter(FunctionNode node);

    boolean exit(FunctionNode node);

    boolean enter(FilterArgumentNode node);

    boolean exit(FilterArgumentNode node);

    boolean visit(LineBreakNode node);

    boolean visit(MultipleLineCommentNode node);

    boolean enter(PropertyNode node);

    boolean exit(PropertyNode node);

    boolean enter(RuleSetNode node);

    boolean exit(RuleSetNode node);

    boolean enter(ScopeNode node);

    boolean exit(ScopeNode node);

    boolean enter(SelectorNode node);

    boolean exit(SelectorNode node);

    boolean enter(SelectorGroupNode node);

    boolean exit(SelectorGroupNode node);

    boolean visit(SelectorSegmentNode node);

    boolean visit(SimpleNode node);

    boolean visit(SingleLineCommentNode node);

    boolean enter(VariableDefinitionNode node);

    boolean exit(VariableDefinitionNode node);

    boolean visit(VariableReferenceNode node);
}
