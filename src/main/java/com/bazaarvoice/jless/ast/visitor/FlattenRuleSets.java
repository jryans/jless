package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.ExpressionNode;
import com.bazaarvoice.jless.ast.ExpressionsNode;
import com.bazaarvoice.jless.ast.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.PropertyNode;
import com.bazaarvoice.jless.ast.RuleSetNode;
import com.bazaarvoice.jless.ast.ScopeNode;
import com.bazaarvoice.jless.ast.SelectorGroupNode;
import com.bazaarvoice.jless.ast.SelectorNode;
import com.bazaarvoice.jless.ast.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.SimpleNode;
import com.bazaarvoice.jless.ast.SingleLineCommentNode;

import java.util.Stack;

/**
 *
 */
public class FlattenRuleSets extends NodeVisitor {
    private Stack<RuleSetNode> _ruleSetStack = new Stack<RuleSetNode>();

    @Override
    public boolean visit(ExpressionNode node) {
        return true;
    }

    @Override
    public boolean visit(ExpressionsNode node) {
        return true;
    }

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        return true;
    }

    @Override
    public boolean visitEnter(PropertyNode node) {
        return true;
    }

    @Override
    public boolean visit(PropertyNode node) {
        return true;
    }

    @Override
    public boolean visitEnter(RuleSetNode node) {
        _ruleSetStack.push(node);
        return true;
    }

    @Override
    public boolean visit(RuleSetNode node) {
        _ruleSetStack.pop();

        // If there is a parent rule set, move this rule set up to be a sibling of its parent.
        if (!_ruleSetStack.empty()) {
            
        }

        return true;
    }

    @Override
    public boolean visitEnter(ScopeNode node) {
        return true;
    }

    @Override
    public boolean visit(ScopeNode node) {
        return true;
    }

    @Override
    public boolean visit(SelectorNode node) {
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
}
