package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.ExpressionGroupNode;
import com.bazaarvoice.jless.ast.node.ExpressionNode;
import com.bazaarvoice.jless.ast.node.ExpressionPhraseNode;
import com.bazaarvoice.jless.ast.node.FunctionNode;
import com.bazaarvoice.jless.ast.node.LineBreakNode;
import com.bazaarvoice.jless.ast.node.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ParametersNode;
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
 *
 */
public abstract class DefaultNodeVisitor implements NodeAdditionVisitor, NodeTraversalVisitor {
    
    protected abstract boolean defaultValue();
    
    // Base visit methods
    // These eliminate the need for visit methods that no visitor uses
    
    @Override
    public boolean add(Node node) {
        return defaultValue();
    }

    @Override
    public boolean enter(Node node) {
        return defaultValue();
    }

    @Override
    public boolean visit(Node node) {
        return defaultValue();
    }

    // Concrete visit methods (NodeAdditionVisitor)


    @Override
    public boolean add(ParametersNode node) {
        return defaultValue();
    }

    @Override
    public boolean add(RuleSetNode node) {
        return defaultValue();
    }

    @Override
    public boolean add(ScopeNode node) {
        return defaultValue();
    }

    @Override
    public boolean add(VariableDefinitionNode node) {
        return defaultValue();
    }

    // Concrete visit methods (NodeTraversalVisitor)

    @Override
    public boolean enter(ExpressionGroupNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(ExpressionGroupNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(ExpressionNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(ExpressionPhraseNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(FunctionNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(FunctionNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(LineBreakNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(PropertyNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(PropertyNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(RuleSetNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(RuleSetNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(ScopeNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(ScopeNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(SelectorNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(SelectorNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(SelectorGroupNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(SelectorGroupNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(SelectorSegmentNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(SimpleNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(SingleLineCommentNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(VariableDefinitionNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(VariableDefinitionNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(VariableReferenceNode node) {
        return defaultValue();
    }
}
