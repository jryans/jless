package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.LineBreakNode;
import com.bazaarvoice.jless.ast.node.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.PropertyNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorGroupNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.node.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.util.MutableTreeUtils;
import org.parboiled.trees.GraphUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 */
public class FlattenNestedRuleSets extends InclusiveNodeVisitor {

    private Stack<RuleSetNode> _ruleSetStack = new Stack<RuleSetNode>();

    private List<Node> _updatedSelectors;
    private List<Node> _selectorWorkingSet;

    @Override
    public boolean enter(PropertyNode node) {
        return false;
    }

    @Override
    public boolean enter(RuleSetNode node) {
        _ruleSetStack.push(node);
        return true;
    }

    @Override
    public boolean visit(RuleSetNode node) {
        _ruleSetStack.pop();

        if (!_ruleSetStack.empty()) {
            // Move this rule set up to be a sibling of its parent with comments that describe the parent
            final RuleSetNode parentRuleSet = _ruleSetStack.get(0);
            MutableTreeUtils.addSiblingAfter(parentRuleSet, surroundWithContext(parentRuleSet, node));

            // If the parent rule set's scope is contains no meaningful content, mark it as invisible
            ScopeNode scope = MutableTreeUtils.getFirstChild(parentRuleSet, ScopeNode.class);
            int scopeChildCount = scope.getChildren().size();
            if (scopeChildCount == 0 || scopeChildCount == MutableTreeUtils.getChildren(scope, LineBreakNode.class).size()) {
                parentRuleSet.setVisible(false);
            }
        }

        return true;
    }

    @Override
    public boolean enter(ScopeNode node) {
        // Don't enter the scope of a nested rule set
        return _ruleSetStack.size() <= 1;
    }

    @Override
    public boolean enter(SelectorGroupNode node) {
        if (_ruleSetStack.size() > 1) {
            // In a nested rule set, reset the list of updated selector nodes and enter
            _updatedSelectors = new ArrayList<Node>();
            return true;
        } else {
            // This visitor ignores the parent's selectors
            return false;
        }
    }

    @Override
    public boolean visit(SelectorGroupNode selectorGroup) {
        if (_ruleSetStack.size() > 1) {
            // In a nested rule set, and the new selectors are now complete

            // Remove the old selectors
            int selectorCount = selectorGroup.getChildren().size();
            for (int i = 0; i < selectorCount; i++) {
                selectorGroup.removeChild(0);
            }

            // Add the new selectors
            for (Node selector : _updatedSelectors) {
                selectorGroup.addChild(selector);
            }
        }

        return true;
    }

    @Override
    public boolean enter(SelectorNode node) {
        // We've reached a selector in a nested rule set, clear the selector working set
        _selectorWorkingSet = new ArrayList<Node>();

        // Visit the parent rule set and clone its selector nodes
        _ruleSetStack.get(0).traverse(new InclusiveNodeVisitor() {
            @Override
            public boolean enter(ScopeNode node) {
                return false; // Don't need to touch the parent's scope
            }

            @Override
            public boolean visit(SelectorNode node) {
                _selectorWorkingSet.add(node.clone());
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean visit(SelectorNode node) {
        // Add the current selector working set to the larger collection of selectors for the nested rule set
        _updatedSelectors.addAll(_selectorWorkingSet);

        return true;
    }

    @Override
    public boolean visit(SelectorSegmentNode segment) {
        // We've reached a selector segment in a nested rule set, append it to all selectors in the working set
        for (Node selector : _selectorWorkingSet) {
            SelectorSegmentNode segmentClone = (SelectorSegmentNode) segment.clone();

            // If this is the first segment and it's not a sub-element selector, use the descendant combinator
            if (segment.getParent().getLatestChildIterator().previousIndex() == 0 && !segment.isSubElementSelector() && segment.getCombinator().equals("")) {
                segmentClone.setCombinator(" "); // TODO: Constants?
            }

            selector.addChild(segmentClone);
        }

        return true;
    }

    private Node[] surroundWithContext(RuleSetNode parent, RuleSetNode node) {
        String parentSelector = MutableTreeUtils.getFirstChild(parent, SelectorGroupNode.class).toString();

        List<Node> nodeList = new ArrayList<Node>();

        // Add rule set header comment
        nodeList.add(new MultipleLineCommentNode(" " + parentSelector + "{ "));

        // Grab line breaks just inside the parent rule set's scope, if any
        Node enterScopeLineBreak = MutableTreeUtils.getFirstChild(parent, ScopeNode.class).getChildren().get(0);
        if (enterScopeLineBreak instanceof LineBreakNode) {
            nodeList.add(enterScopeLineBreak.clone());
        }

        // Add rule set itself
        nodeList.add(node);

        // Add rule set footer comment
        nodeList.add(new MultipleLineCommentNode(" } " + parentSelector));

        // Grab line breaks just at the end of the parent rule set, if any
        Node exitScopeLineBreak = GraphUtils.getLastChild((Node) parent);
        if (exitScopeLineBreak instanceof LineBreakNode) {
            nodeList.add(exitScopeLineBreak.clone());
        }

        return nodeList.toArray(new Node[nodeList.size()]);
    }
}
