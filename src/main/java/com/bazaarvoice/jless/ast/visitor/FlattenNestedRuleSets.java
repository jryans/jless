package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.InternalNode;
import com.bazaarvoice.jless.ast.node.LineBreakNode;
import com.bazaarvoice.jless.ast.node.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.PropertyNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorGroupNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.node.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.util.NodeTreeUtils;
import com.bazaarvoice.jless.ast.util.RandomAccessListIterator;
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

    private int _nodesAddedToParentRuleSet;

    @Override
    public boolean enter(PropertyNode node) {
        return false;
    }

    @Override
    public boolean enter(RuleSetNode node) {
        if (_ruleSetStack.empty()) {
            _nodesAddedToParentRuleSet = 0;
        }

        _ruleSetStack.push(node);
        return true;
    }

    @Override
    public boolean visit(RuleSetNode ruleSet) {
        _ruleSetStack.pop();

        if (!_ruleSetStack.empty()) {
            // Preserve the node's current scope for variable resolution
            final RuleSetNode parentRuleSet = _ruleSetStack.get(0);
            ScopeNode parentScope = NodeTreeUtils.getFirstChild(parentRuleSet, ScopeNode.class);
            ScopeNode scope = NodeTreeUtils.getFirstChild(ruleSet, ScopeNode.class);
            scope.setParentScope(parentScope);

            // Move this rule set up to be a sibling of its parent with comments that describe the parent
            addSiblingAfter(parentRuleSet, surroundWithContext(parentRuleSet, ruleSet));

            // If the parent rule set's scope is contains no meaningful content, mark it as invisible
            int scopeChildCount = parentScope.getChildren().size();
            if (scopeChildCount == 0 || scopeChildCount == NodeTreeUtils.getChildren(parentScope, LineBreakNode.class).size()) {
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
            if (segment.getParent().getLatestChildIterator().previousIndex() == 0 && !segment.isSubElementSelector() && segment.getCombinator().equals(SelectorSegmentNode.NO_COMBINATOR)) {
                segmentClone.setCombinator(SelectorSegmentNode.DESCENDANT_COMBINATOR);
            }

            selector.addChild(segmentClone);
        }

        return true;
    }

    /**
     * Add the input nodes after the current node in its parent's list of children, but yet
     * also ensure that they will be included in the current iteration of those children.
     */
    private void addSiblingAfter(Node node, List<Node> siblings) {
        InternalNode parent = node.getParent();

        // Add the sibling nodes after the current node (also the parent iterator's current node)
        RandomAccessListIterator<Node> childIterator = parent.getLatestChildIterator();
        int index = childIterator.nextIndex() + _nodesAddedToParentRuleSet;
        for (Node sibling : siblings) {
            childIterator.add(index++, sibling);
        }

        // If the offset was 0, then the iterator advanced with each add
        if (_nodesAddedToParentRuleSet == 0) {
            // Rewind the iterator so that the added nodes are visited
            for (Node sibling : siblings) {
                childIterator.previous();
            }
        }

        // Update offset for future add operations
        _nodesAddedToParentRuleSet += siblings.size();
    }

    private List<Node> surroundWithContext(RuleSetNode parent, RuleSetNode node) {
        String parentSelector = NodeTreeUtils.getFirstChild(parent, SelectorGroupNode.class).toString();

        List<Node> nodeList = new ArrayList<Node>();

        // Add rule set header comment
        nodeList.add(new MultipleLineCommentNode(" " + parentSelector + " { "));

        // Grab line breaks just inside the parent rule set's scope, if any
        Node enterScopeLineBreak = NodeTreeUtils.getFirstChild(parent, ScopeNode.class).getChildren().get(0);
        if (enterScopeLineBreak instanceof LineBreakNode) {
            nodeList.add(enterScopeLineBreak);
        }

        // Add rule set itself
        nodeList.add(node);

        // Add rule set footer comment
        nodeList.add(new MultipleLineCommentNode(" } " + parentSelector));

        // Grab line breaks just at the end of the parent rule set, if any
        Node exitScopeLineBreak = GraphUtils.getLastChild((Node) parent);
        if (exitScopeLineBreak instanceof LineBreakNode) {
            nodeList.add(exitScopeLineBreak);
        }

        return nodeList;
    }
}
