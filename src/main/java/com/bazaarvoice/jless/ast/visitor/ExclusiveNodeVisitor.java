package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.ast.ExpressionGroupNode;
import com.bazaarvoice.jless.ast.ExpressionNode;
import com.bazaarvoice.jless.ast.ExpressionsNode;
import com.bazaarvoice.jless.ast.LineBreakNode;
import com.bazaarvoice.jless.ast.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.PropertyNode;
import com.bazaarvoice.jless.ast.RuleSetNode;
import com.bazaarvoice.jless.ast.ScopeNode;
import com.bazaarvoice.jless.ast.SelectorGroupNode;
import com.bazaarvoice.jless.ast.SelectorNode;
import com.bazaarvoice.jless.ast.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.SimpleNode;
import com.bazaarvoice.jless.ast.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.VariableDefinitionNode;

/**
 * Defaults to visiting no nodes.
 */
public class ExclusiveNodeVisitor implements NodeVisitor {

    // Base visit methods
    // These eliminate the need for visit methods that no visitor uses

    @Override
    public boolean visitEnter(Node node) {
        return false;
    }

    @Override
    public boolean visit(Node node) {
        return false;
    }

    // Concrete visit methods

    @Override
    public boolean visitEnter(ExpressionGroupNode node) {
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
    public boolean visitEnter(PropertyNode node) {
        return false;
    }

    @Override
    public boolean visit(PropertyNode node) {
        return false;
    }

    @Override
    public boolean visitEnter(RuleSetNode node) {
        return false;
    }

    @Override
    public boolean visit(RuleSetNode node) {
        return false;
    }

    @Override
    public boolean visitEnter(ScopeNode node) {
        return false;
    }

    @Override
    public boolean visit(ScopeNode node) {
        return false;
    }

    @Override
    public boolean visitEnter(SelectorNode node) {
        return false;
    }

    @Override
    public boolean visit(SelectorNode node) {
        return false;
    }

    @Override
    public boolean visitEnter(SelectorGroupNode node) {
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
    public boolean visitEnter(VariableDefinitionNode node) {
        return false;
    }

    @Override
    public boolean visit(VariableDefinitionNode node) {
        return false;
    }
}
