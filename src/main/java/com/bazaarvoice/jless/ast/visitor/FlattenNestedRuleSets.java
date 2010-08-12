package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.ast.RuleSetNode;
import com.bazaarvoice.jless.ast.ScopeNode;
import com.bazaarvoice.jless.ast.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.util.MutableTreeUtils;
import com.bazaarvoice.jless.print.Printer;

import java.util.Stack;

/**
 *
 */
public class FlattenNestedRuleSets extends BaseNodeVisitor {

    private Stack<RuleSetNode> _ruleSetStack = new Stack<RuleSetNode>();

    @Override
    public boolean visitEnter(RuleSetNode node) {
        _ruleSetStack.push(node);
        return true;
    }

    @Override
    public boolean visit(RuleSetNode node) {
        _ruleSetStack.pop();

        if (!_ruleSetStack.empty()) {
            // There is a parent rule set, move this rule set up to be a sibling of its parent with comments that describe it.
            MutableTreeUtils.addSiblingAfter(_ruleSetStack.peek(), surroundWithContext(node));
        }

        return true;
    }

    @Override
    public boolean visitEnter(ScopeNode node) {
        // Don't enter the scope of a nested rule set
        return _ruleSetStack.size() <= 1;
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

        node.accept(contextPrinter);

        return new Node[] {
                new SingleLineCommentNode(" " + contextPrinter.toString() + "{"),
                node,
                new SingleLineCommentNode(" } " + contextPrinter.toString())
        };
    }
}
