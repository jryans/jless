package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ExpressionGroupNode;
import com.bazaarvoice.jless.ast.node.ExpressionNode;
import com.bazaarvoice.jless.ast.node.ExpressionsNode;
import com.bazaarvoice.jless.ast.node.LineBreakNode;
import com.bazaarvoice.jless.ast.node.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.node.PropertyNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorGroupNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.node.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.node.SimpleNode;
import com.bazaarvoice.jless.ast.node.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.node.VariableDefinitionNode;

/**
 * Defaults to visiting every node.
 */
public class InclusiveNodeVisitor implements NodeAdditionVisitor, NodeTraversalVisitor {

    private static final InclusiveNodeVisitor SINGLETON = new InclusiveNodeVisitor();

    public static InclusiveNodeVisitor getInstance() {
        return SINGLETON;
    }

    // Base visit methods
    // These eliminate the need for visit methods that no visitor uses

    @Override
    public boolean add(Node node) {
        return true;
    }

    @Override
    public boolean enter(Node node) {
        return true;
    }

    @Override
    public boolean visit(Node node) {
        return true;
    }

    // Concrete visit methods (NodeAdditionVisitor)

    @Override
    public boolean add(VariableDefinitionNode node) {
        return true;
    }

    // Concrete visit methods (NodeTraversalVisitor)

    @Override
    public boolean enter(ExpressionGroupNode node) {
        return true;
    }

    @Override
    public boolean visit(ExpressionGroupNode node) {
        return true;
    }

    @Override
    public boolean visit(ExpressionNode node) {
        return true;
    }

    @Override
    public boolean visit(ExpressionsNode node) {
        return true;
    }

    @Override
    public boolean visit(LineBreakNode node) {
        return true;
    }

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        return true;
    }

    @Override
    public boolean enter(PropertyNode node) {
        return true;
    }

    @Override
    public boolean visit(PropertyNode node) {
        return true;
    }

    @Override
    public boolean enter(RuleSetNode node) {
        return true;
    }

    @Override
    public boolean visit(RuleSetNode node) {
        return true;
    }

    @Override
    public boolean enter(ScopeNode node) {
        return true;
    }

    @Override
    public boolean visit(ScopeNode node) {
        return true;
    }

    @Override
    public boolean enter(SelectorNode node) {
        return true;
    }

    @Override
    public boolean visit(SelectorNode node) {
        return true;
    }

    @Override
    public boolean enter(SelectorGroupNode node) {
        return true;
    }

    @Override
    public boolean visit(SelectorGroupNode node) {
        return true;
    }

    @Override
    public boolean visit(SelectorSegmentNode node) {
        return true;
    }

    @Override
    public boolean visit(SimpleNode node) {
        return true;
    }

    @Override
    public boolean visit(SingleLineCommentNode node) {
        return true;
    }

    @Override
    public boolean enter(VariableDefinitionNode node) {
        return true;
    }

    @Override
    public boolean visit(VariableDefinitionNode node) {
        return true;
    }
}
