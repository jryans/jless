package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.ExpressionNode;
import com.bazaarvoice.jless.ast.ExpressionsNode;
import com.bazaarvoice.jless.ast.LineBreakNode;
import com.bazaarvoice.jless.ast.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.ast.PropertyNode;
import com.bazaarvoice.jless.ast.RuleSetNode;
import com.bazaarvoice.jless.ast.ScopeNode;
import com.bazaarvoice.jless.ast.SelectorGroupNode;
import com.bazaarvoice.jless.ast.SelectorNode;
import com.bazaarvoice.jless.ast.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.SimpleNode;
import com.bazaarvoice.jless.ast.SingleLineCommentNode;
import org.parboiled.trees.GraphUtils;

import java.util.List;

// TODO: Add all input new lines to AST and don't add any of my own
public class Printer extends BaseNodeVisitor {

    private static final int INDENT_STEP = 2;

    private StringBuilder _sb = new StringBuilder();
    private int _indent = 0;

    // Node output

    @Override
    public boolean visit(ExpressionNode node) {
        if (GraphUtils.getLastChild(node.getParent()) != node) {
            print(' ');
        }
        return true;
    }

    @Override
    public boolean visit(ExpressionsNode node) {
        if (GraphUtils.getLastChild(node.getParent()) != node) {
            print(", ");
        }
        return true;
    }

    @Override
    public boolean visit(LineBreakNode node) {
        for (int i = 0; i < node.getLineBreaks(); i++) {
            printLine();
        }
        return true;
    }

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        print("/*").print(node.getValue()).print("*/");
        return true;
    }

    @Override
    public boolean visitEnter(PropertyNode node) {
        print(node.getName()).print(": ");
        return true;
    }

    @Override
    public boolean visit(PropertyNode node) {
        print(";");
        if (node.getParent().getChildren().size() > 1 && node.getParent().getLatestChildIterator().hasNext()) {
            print(' ');
        }
        return true;
    }

    @Override
    public boolean visitEnter(RuleSetNode node) {
        return node.isVisible();
    }

    @Override
    public boolean visitEnter(ScopeNode node) {
        if (node.getParent() != null) {
            print("{");
            List<Node> children = node.getChildren();
            if (children.isEmpty()) {
                // do nothing
            } else if (children.size() == 1 && children.get(0) instanceof RuleSetNode) {
                addIndent().printLine().printIndent();
            } else {
//                print(' ');
            }
        }
        return true;
    }

    @Override
    public boolean visit(ScopeNode node) {
        if (node.getParent() != null) {
            List<Node> children = node.getChildren();
            if (children.isEmpty()) {
                // do nothing
            } else if (children.size() == 1 && children.get(0) instanceof RuleSetNode) {
                removeIndent().printLine().printIndent();
            } else {
//                print(' ');
            }
            print('}');
            if (node.getParent().getParent().getLatestChildIterator().hasNext()) {
//                printLine().printIndent();
            }
        }
        return true;
    }

    @Override
    public boolean visit(SelectorNode node) {
        if (GraphUtils.getLastChild(node.getParent()) != node) {
            print(", ");
        }
        return true;
    }

    @Override
    public boolean visit(SelectorGroupNode node) {
        print(' ');
        return true;
    }

    @Override
    public boolean visit(SelectorSegmentNode node) {
        print(node.getCombinator()).print(node.getSimpleSelector());
        return true;
    }

    @Override
    public boolean visit(SimpleNode node) {
        print(node.getValue());
        return true;
    }

    @Override
    public boolean visit(SingleLineCommentNode node) {
        print("//").print(node.getValue()).print('\n');
        return true;
    }

    // Printing methods

    private Printer print(String s) {
        _sb.append(s);
        return this;
    }

    private Printer print(Character c) {
        _sb.append(c);
        return this;
    }

    private Printer printIndent() {
        for (int i = 0; i < _indent; i++) {
            _sb.append(' ');
        }
        return this;
    }

    private Printer printLine() {
        _sb.append('\n');
        return this;
    }

    private Printer addIndent() {
        _indent += INDENT_STEP;
        return this;
    }

    private Printer removeIndent() {
        _indent -= INDENT_STEP;
        return this;
    }

    @Override
    public String toString() {
        return _sb.toString();
    }
}
