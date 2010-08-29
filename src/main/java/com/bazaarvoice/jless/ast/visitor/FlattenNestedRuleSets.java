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
import com.bazaarvoice.jless.ast.util.NodeTreeUtils;
import com.bazaarvoice.jless.ast.util.RandomAccessListIterator;
import org.parboiled.trees.GraphUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This visitor flattens nested rule sets since nesting is not allowed in CSS.  When a rule set move up the
 * hierarchy in this manner, each of its selectors is combined with the selectors from the parent rule set.
 *
 * Example input:
 *   .cat, .dog {
 *       :hover, .bob {
 *           color: blue;
 *       }
 *   }
 *
 * Resulting output (comments not shown):
 *  .cat:hover, .dog:hover, .cat .bob, .dog .bob {
        color: blue;
 *  }
 */
public class FlattenNestedRuleSets extends InclusiveNodeVisitor {

    private Stack<RuleSetNode> _ruleSetStack = new Stack<RuleSetNode>();
    private List<Node> _updatedSelectors;
    private int _nodesAddedToParentRuleSet;

    @Override
    public boolean enter(RuleSetNode node) {
        // If entering the parent rule set
        if (_ruleSetStack.empty()) {
            _nodesAddedToParentRuleSet = 0;
        }

        _ruleSetStack.push(node);
        return true;
    }

    @Override
    public boolean exit(RuleSetNode ruleSet) {
        _ruleSetStack.pop();

        // If exiting the nested rule set
        if (!_ruleSetStack.empty()) {
            // Preserve the rule set's current scope for variable resolution by setting the parent scope link
            final RuleSetNode parentRuleSet = _ruleSetStack.get(0);
            ScopeNode parentScope = NodeTreeUtils.getFirstChild(parentRuleSet, ScopeNode.class);
            ScopeNode scope = NodeTreeUtils.getFirstChild(ruleSet, ScopeNode.class);
            scope.setParentScope(parentScope);

            // Move this rule set up to be a sibling of its parent with comments that describe the parent
            addSiblingAfter(parentRuleSet, surroundWithContext(parentRuleSet, ruleSet));

            // TODO: replace with line breaks you contain
            // If the parent rule set's scope contains no meaningful content, mark it as invisible
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
    public boolean exit(SelectorGroupNode selectorGroup) {
        if (_ruleSetStack.size() > 1) {
            // In a nested rule set, and the new selectors are now complete

            // Replace old selectors
            selectorGroup.clearChildren();
            selectorGroup.addChildren(_updatedSelectors);
        }

        return true;
    }

    @Override
    public boolean enter(final SelectorNode selector) {
        // We've reached a selector in a nested rule set, create a new selector working set
        final List<SelectorNode> selectorWorkingSet = new ArrayList<SelectorNode>();

        // Visit the parent rule set and clone its selector nodes
        _ruleSetStack.get(0).traverse(new InclusiveNodeVisitor() {
            @Override
            public boolean enter(ScopeNode node) {
                return false; // Don't need to touch the parent's scope
            }

            @Override
            public boolean exit(SelectorNode parentSelector) {
                SelectorNode parentSelectorClone = parentSelector.clone();

                // Add the current selector from the nested rule set to the cloned selector node from the parent.
                // SelectorNode listens for additions of other SelectorNodes, and will absorb its children properly.
                parentSelectorClone.addChild(selector.clone());

                selectorWorkingSet.add(parentSelectorClone);
                return true;
            }
        });

        // Add the current selector working set to the larger collection of selectors for the nested rule set
        _updatedSelectors.addAll(selectorWorkingSet);

        return false;
    }

    @Override
    public boolean enter(PropertyNode node) {
        return false;
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
        nodeList.add(new MultipleLineCommentNode(" } " + parentSelector + " "));

        // Grab line breaks just at the end of the parent rule set, if any
        Node exitScopeLineBreak = GraphUtils.getLastChild((Node) parent);
        if (exitScopeLineBreak instanceof LineBreakNode) {
            nodeList.add(exitScopeLineBreak);
        }

        return nodeList;
    }
}
