package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.PropertyNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorGroupNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.util.NodeTreeUtils;
import com.bazaarvoice.jless.ast.util.RandomAccessListIterator;

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

    private RuleSetNode _parentRuleSet;
    private SelectorGroupNode _parentSelectorGroup;

    @Override
    public boolean enter(ScopeNode scope) {
        // If there's already a parent rule set, then use a new visitor for nested rule sets in this scope.
        if (_parentRuleSet != null) {
            scope.traverse(new NestedRuleSetVisitor(_parentSelectorGroup, scope));

            // Don't enter the scope in this visitor
            return false;
        }

        return true;
    }

    @Override
    public boolean enter(RuleSetNode ruleSet) {
        // Entering the parent rule set
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
        // This visitor doesn't use the parent's selector, but passes it down to the nested visitor
        _parentSelectorGroup = node;
        return false;
    }

    private static class NestedRuleSetVisitor extends InclusiveNodeVisitor {

        private final SelectorGroupNode _parentSelectorGroup;
        private final ScopeNode _parentScope;

        private boolean _foundNestedRuleSets = false;
        private SelectorGroupNode _nestedSelectorGroup;
        private PropertyGroup _currentPropertyGroup = null;
        private Stack<PropertyGroup> _propertyGroups = new Stack<PropertyGroup>();

        private NestedRuleSetVisitor(SelectorGroupNode parentSelectorGroup, ScopeNode parentScope) {
            _parentSelectorGroup = parentSelectorGroup;
            _parentScope = parentScope;
        }

        @Override
        public boolean enter(RuleSetNode node) {
            _foundNestedRuleSets = true;
            recordCurrentPropertyGroup();
            return true;
        }

        @Override
        public boolean enter(SelectorGroupNode node) {
            _nestedSelectorGroup = node;
            return true;
        }

        @Override
        public boolean enter(final SelectorNode selector) {
            final RandomAccessListIterator<Node> selectorGroupIterator = selector.getParent().getLatestChildIterator();

            // Remove current selector node from its parent
            selectorGroupIterator.remove();

            // Visit the parent selector group and clone its selector nodes
            _parentSelectorGroup.traverse(new InclusiveNodeVisitor() {
                @Override
                public boolean exit(SelectorNode parentSelector) {
                    SelectorNode parentSelectorClone = parentSelector.clone();

                    // Add the current selector from the nested rule set to the cloned selector node from the parent.
                    // SelectorNode listens for additions of other SelectorNodes, and will absorb its children properly.
                    parentSelectorClone.addChild(selector.clone());

                    // Add the new selector to the selector group
                    selectorGroupIterator.add(parentSelectorClone);
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
        public boolean exit(ScopeNode scope) {
            if (scope == _parentScope) { // Leaving the initial scope (inside the parent rule set)
                // Nothing to do if there are no nested rule sets present
                if (!_foundNestedRuleSets) {
                    return true;
                }

                // Record a possible property group at the end of the scope
                recordCurrentPropertyGroup();

                // Surround all properties
                surroundPropertyGroups();

                // Hide the parent rule set's selector and scope brackets
                _parentSelectorGroup.setVisible(false);
                _parentScope.setBracketsDisplayed(false);
            } else { // Leaving a nested rule set's scope
                scope.traverse(new NestedRuleSetVisitor(_nestedSelectorGroup, scope));
            }

            return true;
        }

        @Override
        public boolean enter(PropertyNode node) {
            // Get the current node's index in its parent
            int index = node.getParent().getLatestChildIterator().previousIndex();

            if (_currentPropertyGroup == null) {
                _currentPropertyGroup = new PropertyGroup(index);
            }

            _currentPropertyGroup.setEnd(index);

            return false;
        }

        @Override
        public boolean visitInvisible(Node node) {
            return false;
        }

        private void recordCurrentPropertyGroup() {
            // Only have work to do if we have encountered property nodes previously
            if (_currentPropertyGroup == null) {
                return;
            }

            // Note this property group in the map
            _propertyGroups.push(_currentPropertyGroup);

            _currentPropertyGroup = null;
        }

        private void surroundPropertyGroups() {
            while (!_propertyGroups.empty()) {
                PropertyGroup group = _propertyGroups.pop();

                // Construct a simple rule set using the parent's selector
                RuleSetNode propertyRuleSet = new RuleSetNode();
                propertyRuleSet.addChild(NodeTreeUtils.filterWhiteSpace(_parentSelectorGroup.clone()));

                // Move all the properties into this new rule set
                List<Node> propertyNodes = new ArrayList<Node>(_parentScope.getChildren().subList(group.getStart(), group.getEnd() + 1));
                propertyRuleSet.addChild(new ScopeNode(propertyNodes));

                // Insert the property rule set
                _parentScope.addChild(group.getStart(), propertyRuleSet);
            }
        }

        private static class PropertyGroup {
            private int _start;
            private int _end;

            private PropertyGroup(int start) {
                _start = start;
            }

            public int getStart() {
                return _start;
            }

            public int getEnd() {
                return _end;
            }

            public void setEnd(int end) {
                _end = end;
            }
        }
    }
}
