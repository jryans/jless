package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ExpressionGroupNode;
import com.bazaarvoice.jless.ast.node.ExpressionNode;
import com.bazaarvoice.jless.ast.node.ExpressionsNode;
import com.bazaarvoice.jless.ast.node.LineBreakNode;
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
 * Defaults to visiting no nodes.
 */
public class ExclusiveNodeVisitor implements NodeAdditionVisitor, NodeTraversalVisitor {

    private static final ExclusiveNodeVisitor SINGLETON = new ExclusiveNodeVisitor();

    public static ExclusiveNodeVisitor getInstance() {
        return SINGLETON;
    }

    // Base visit methods
    // These eliminate the need for visit methods that no visitor uses

    @Override
    public boolean add(Node node) {
        return false;
    }

    @Override
    public boolean enter(Node node) {
        return false;
    }

    @Override
    public boolean visit(Node node) {
        return false;
    }
    
    // Concrete visit methods (NodeAdditionVisitor)
    
    @Override
    public boolean add(VariableDefinitionNode node) {
        return false;
    }

    @Override
    public boolean enter(ExpressionGroupNode node) {
        return false;
    }

    @Override
    public boolean visit(ExpressionGroupNode node) {
        return false;
    }

    @Override
    public boolean visit(ExpressionNode node) {
        return false;
    }

    @Override
    public boolean visit(ExpressionsNode node) {
        return false;
    }

    @Override
    public boolean visit(LineBreakNode node) {
        return false;
    }

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        return false;
    }

    @Override
    public boolean enter(PropertyNode node) {
        return false;
    }

    @Override
    public boolean visit(PropertyNode node) {
        return false;
    }

    @Override
    public boolean enter(RuleSetNode node) {
        return false;
    }

    @Override
    public boolean visit(RuleSetNode node) {
        return false;
    }

    @Override
    public boolean enter(ScopeNode node) {
        return false;
    }

    @Override
    public boolean visit(ScopeNode node) {
        return false;
    }

    @Override
    public boolean enter(SelectorNode node) {
        return false;
    }

    @Override
    public boolean visit(SelectorNode node) {
        return false;
    }

    @Override
    public boolean enter(SelectorGroupNode node) {
        return false;
    }

    @Override
    public boolean visit(SelectorGroupNode node) {
        return false;
    }

    @Override
    public boolean visit(SelectorSegmentNode node) {
        return false;
    }

    @Override
    public boolean visit(SimpleNode node) {
        return false;
    }

    @Override
    public boolean visit(SingleLineCommentNode node) {
        return false;
    }

    @Override
    public boolean enter(VariableDefinitionNode node) {
        return false;
    }

    @Override
    public boolean visit(VariableDefinitionNode node) {
        return false;
    }
}
