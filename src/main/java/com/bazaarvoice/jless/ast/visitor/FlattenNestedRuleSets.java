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
        if (_ruleSetStack.empty()) {
            _ruleSetStack.push(node);
            return true;
        }

        /*NodeVisitor getSelectorGroupNode = new NodeVisitor() {

        }*/

        // Get this node's current selecto

        // There is a parent rule set, move this rule set up to be a sibling of its parent with comments that describe it.
        MutableTreeUtils.addSiblingAfter(_ruleSetStack.peek(), surroundWithContext(node));
            
        _ruleSetStack.push(node);
        return false; // Don't need to descend, rule set will be revisited in new location
    }

    @Override
    public boolean visit(RuleSetNode node) {
        _ruleSetStack.pop();
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

        node.accept(contextPrinter);

        return new Node[] {
                new SingleLineCommentNode(" " + contextPrinter.toString() + "{"),
                node,
                new SingleLineCommentNode(" } " + contextPrinter.toString())
        };
    }
}
