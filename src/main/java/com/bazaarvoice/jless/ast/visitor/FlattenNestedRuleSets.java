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

    private RuleSetNode _parentRuleSet;
    private int _nodesAddedToParentRuleSet;

    @Override
    public boolean enter(ScopeNode scope) {
        // If there's already a parent rule set, then use a new visitor for nested rule sets in this scope.
        if (_parentRuleSet != null) {
            scope.traverse(new NestedRuleSetVisitor(scope));

            // Don't enter the scope in this visitor
            return false;
        }

        return true;
    }

    @Override
    public boolean enter(RuleSetNode ruleSet) {
        // Entering the parent rule set
        _nodesAddedToParentRuleSet = 0;
        _parentRuleSet = ruleSet;
        return true;
    }

    @Override
    public boolean exit(RuleSetNode ruleSet) {
        _parentRuleSet = null;
        return true;
    }

    @Override
    public boolean enter(SelectorGroupNode node) {
        // This visitor ignores the parent's selectors
        return false;
    }

    private class NestedRuleSetVisitor extends InclusiveNodeVisitor {

        private ScopeNode _parentScope;
        private List<Node> _updatedSelectors;

        private NestedRuleSetVisitor(ScopeNode parentScope) {
            _parentScope = parentScope;
        }

        @Override
        public boolean exit(RuleSetNode ruleSet) {
            // Preserve the rule set's current scope for variable resolution by setting the parent scope link
            ScopeNode scope = NodeTreeUtils.getFirstChild(ruleSet, ScopeNode.class);
            scope.setParentScope(_parentScope);

            // Move this rule set up to be a sibling of its parent with comments that describe the parent
            addSiblingAfter(_parentRuleSet, surroundWithContext(_parentRuleSet, ruleSet));

            // If the parent rule set's scope contains no meaningful content, mark it as invisible
            if (_parentScope.getChildren().size() ==
                    (NodeTreeUtils.getChildren(_parentScope, LineBreakNode.class).size() + NodeTreeUtils.getChildrenWithVisibility(_parentScope, false).size())) {
                _parentRuleSet.setVisible(false);
            }

            return true;
        }

        @Override
        public boolean enter(SelectorGroupNode node) {
            // Reset the list of updated selector nodes and enter
            _updatedSelectors = new ArrayList<Node>();
            return true;
        }

        @Override
        public boolean exit(SelectorGroupNode selectorGroup) {
            // Replace old selectors
            selectorGroup.clearChildren();
            selectorGroup.addChildren(_updatedSelectors);
            return true;
        }

        @Override
        public boolean enter(final SelectorNode selector) {
            // Visit the parent rule set and clone its selector nodes
            _parentRuleSet.traverse(new InclusiveNodeVisitor() {
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

                    _updatedSelectors.add(parentSelectorClone);
                    return true;
                }
            });

            return false;
        }

        @Override
        public boolean enter(ScopeNode node) {
            // Only enter the initial scope (inside the parent rule set)
            return node == _parentScope;
        }

        @Override
        public boolean enter(PropertyNode node) {
            return false;
        }

        @Override
        public boolean visitInvisible(Node node) {
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

            // If nodes added is 0, then the iterator will advance with each add
            if (_nodesAddedToParentRuleSet == 0) {
                // The iterator adds each element at its current position and increments
                for (Node sibling : siblings) {
                    childIterator.add(sibling);
                    _nodesAddedToParentRuleSet++;
                }

                // Rewind the iterator so that the added nodes are visited
                for (Node sibling : siblings) {
                    childIterator.previous();
                }
            } else {
                // The iterator won't advance, so increment the relative offset each time
                for (Node sibling : siblings) {
                    childIterator.add(_nodesAddedToParentRuleSet++, sibling);
                }
            }
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
}
