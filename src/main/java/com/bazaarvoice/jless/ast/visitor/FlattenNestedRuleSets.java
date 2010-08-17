package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.ast.PropertyNode;
import com.bazaarvoice.jless.ast.RuleSetNode;
import com.bazaarvoice.jless.ast.ScopeNode;
import com.bazaarvoice.jless.ast.SelectorGroupNode;
import com.bazaarvoice.jless.ast.SelectorNode;
import com.bazaarvoice.jless.ast.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.util.MutableTreeUtils;

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
    public boolean visitEnter(PropertyNode node) {
        return false;
    }

    @Override
    public boolean visitEnter(RuleSetNode node) {
        _ruleSetStack.push(node);
        return true;
    }

    @Override
    public boolean visit(RuleSetNode node) {
        _ruleSetStack.pop();

        if (!_ruleSetStack.empty()) {
            // Move this rule set up to be a sibling of its parent with comments that describe the parent
            final RuleSetNode parentRuleSet = _ruleSetStack.get(0);
            MutableTreeUtils.addSiblingAfter(parentRuleSet, surroundWithContext(node));

            // If the parent rule set's scope is now empty, mark it as invisible
            parentRuleSet.accept(new InclusiveNodeVisitor() {
                @Override
                public boolean visitEnter(SelectorGroupNode node) {
                    return false; // Not interested in selectors
                }

                @Override
                public boolean visit(ScopeNode scope) {
                    if (scope.getChildren().size() == 0) {
                        parentRuleSet.setVisible(false);
                    }
                    return false; // Don't go anywhere else
                }
            });
        }

        return true;
    }

    @Override
    public boolean visitEnter(ScopeNode node) {
        // Don't enter the scope of a nested rule set
        return _ruleSetStack.size() <= 1;
    }

    @Override
    public boolean visitEnter(SelectorGroupNode node) {
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
    public boolean visitEnter(SelectorNode node) {
        // We've reached a selector in a nested rule set, clear the selector working set
        _selectorWorkingSet = new ArrayList<Node>();

        // Visit the parent rule set and clone its selector nodes
        _ruleSetStack.get(0).accept(new InclusiveNodeVisitor() {
            @Override
            public boolean visitEnter(ScopeNode node) {
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

    private Node[] surroundWithContext(RuleSetNode node) {
        Printer contextPrinter = new Printer() {
            @Override
            public boolean visitEnter(ScopeNode node) {
                return false; // Don't process children of the rule set
            }

            @Override
            public boolean visit(ScopeNode node) {
                return false;
            }
        };

        _ruleSetStack.get(0).accept(contextPrinter);
        String parentSelector = contextPrinter.toString();

        return new Node[] {
                new SingleLineCommentNode(" " + parentSelector + "{"),
                node,
                new SingleLineCommentNode(" } " + parentSelector)
        };
    }
}
