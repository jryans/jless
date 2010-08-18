package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.ExpressionsNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ExpressionNode;
import com.bazaarvoice.jless.ast.node.LineBreakNode;
import com.bazaarvoice.jless.ast.node.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.node.PropertyNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorGroupNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.node.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.node.SimpleNode;
import com.bazaarvoice.jless.ast.node.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.node.VariableDefinitionNode;
import com.bazaarvoice.jless.ast.util.MutableTreeUtils;
import org.parboiled.trees.GraphUtils;

import java.util.List;

public class Printer extends InclusiveNodeVisitor {

    private static final int INDENT_STEP = 4;

    private StringBuilder _sb = new StringBuilder();
    private int _indent = 0;
    private boolean _lastPrintedIndent = false;

    // Node output

    @Override
    public boolean visit(ExpressionNode node) {
        if (MutableTreeUtils.parentHasNext(node)) {
            print(' ');
        }
        return true;
    }

    @Override
    public boolean visit(ExpressionsNode node) {
        if (MutableTreeUtils.parentHasNext(node)) {
            print(", ");
        }
        return true;
    }

    @Override
    public boolean visit(LineBreakNode node) {
        for (int i = 0; i < node.getLineBreaks(); i++) {
            printLine();
        }
        if (node.getLineBreaks() > 0) {
            printIndent();
        }
        return true;
    }

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        print("/*").print(node.getValue()).print("*/");
        return true;
    }

    @Override
    public boolean enter(PropertyNode node) {
        print(node.getName()).print(": ");
        return true;
    }

    @Override
    public boolean visit(PropertyNode node) {
        print(";");
        if (node.getParent().getChildren().size() > 1 && MutableTreeUtils.parentHasNext(node)) {
            print(' ');
        }
        return true;
    }

    @Override
    public boolean enter(RuleSetNode node) {
        return node.isVisible();
    }

    @Override
    public boolean enter(ScopeNode node) {
        if (node.getParent() != null) {
            print("{");
            List<Node> children = node.getChildren();
            if (children.isEmpty()) {
                // do nothing
            } else {
                addIndent();
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
            } else {
                removeIndent();
//                print(' ');
            }
            deleteIndent().print('}');
        }
        return true;
    }

    @Override
    public boolean visit(SelectorNode node) {
        if (GraphUtils.getLastChild((Node) node.getParent()) != node) {
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

    @Override
    public boolean enter(VariableDefinitionNode node) {
        print(node.getName()).print(": ");
        return true;
    }

    @Override
    public boolean visit(VariableDefinitionNode node) {
        print(";");
        if (node.getParent().getChildren().size() > 1 && MutableTreeUtils.parentHasNext(node)) {
            print(' ');
        }
        return true;
    }

    // Printing methods

    private Printer print(String s) {
        _sb.append(s);
        _lastPrintedIndent = false;
        return this;
    }

    private Printer print(Character c) {
        _sb.append(c);
        _lastPrintedIndent = false;
        return this;
    }

    private Printer printIndent() {
        for (int i = 0; i < _indent; i++) {
            _sb.append(' ');
        }
        _lastPrintedIndent = true;
        return this;
    }

    private Printer printLine() {
        _sb.append('\n');
        _lastPrintedIndent = false;
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

    private Printer deleteIndent() {
        if (_lastPrintedIndent) {
            _sb.delete(_sb.length() - INDENT_STEP, _sb.length());
        }
        return this;
    }

    @Override
    public String toString() {
        return _sb.toString();
    }
}
