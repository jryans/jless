package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.ExpressionGroupNode;
import com.bazaarvoice.jless.ast.node.ExpressionPhraseNode;
import com.bazaarvoice.jless.ast.node.FunctionNode;
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
import com.bazaarvoice.jless.ast.node.VariableReferenceNode;
import com.bazaarvoice.jless.ast.util.NodeTreeUtils;

import java.util.List;

public class ParsedPrinter extends InclusiveNodeVisitor {

    private static final int INDENT_STEP = 4;

    private StringBuilder _sb = new StringBuilder();
    private int _indent = 0;
    private boolean _lastPrintedIndent = false;

    // Node output

    @Override
    public boolean visit(ExpressionGroupNode node) {
        if (NodeTreeUtils.parentHasNext(node)) {
            print(", ");
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(ExpressionNode node) {
        if (NodeTreeUtils.parentHasNext(node)) {
            print(' ');
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(ExpressionPhraseNode node) {
        if (NodeTreeUtils.parentHasNext(node)) {
            print(", ");
        }
        return super.visit(node);
    }

    @Override
    public boolean enter(FunctionNode node) {
        print(node.getName()).print('(');
        return super.enter(node);
    }

    @Override
    public boolean visit(FunctionNode node) {
        print(')');
        return super.visit(node);
    }

    @Override
    public boolean visit(LineBreakNode node) {
        for (int i = 0; i < node.getLineBreaks(); i++) {
            printLine();
        }
        if (node.getLineBreaks() > 0) {
            printIndent();
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        print("/*").print(node.getValue()).print("*/");
        return super.visit(node);
    }

    @Override
    public boolean enter(PropertyNode node) {
        print(node.getName()).print(": ");
        return super.enter(node);
    }

    @Override
    public boolean visit(PropertyNode node) {
        print(";");
        if (node.getParent().getChildren().size() > 1 && NodeTreeUtils.parentHasNext(node)) {
            print(' ');
        }
        return super.visit(node);
    }

    @Override
    public boolean enter(RuleSetNode node) {
        return node.isVisible();
    }

    @Override
    public boolean enter(ScopeNode node) {
        if (node.getParent() != null) {
            print(" {");
            List<Node> children = node.getChildren();
            if (children.isEmpty()) {
                // do nothing
            } else {
                addIndent();
            }
        }
        return super.enter(node);
    }

    @Override
    public boolean visit(ScopeNode node) {
        if (node.getParent() != null) {
            List<Node> children = node.getChildren();
            if (children.isEmpty()) {
                // do nothing
            } else {
                removeIndent();
            }
            deleteIndent().print('}');
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(SelectorNode node) {
        if (NodeTreeUtils.parentHasNext(node)) {
            print(", ");
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(SelectorGroupNode node) {
        return super.visit(node);
    }

    @Override
    public boolean visit(SelectorSegmentNode node) {
        print(node.getCombinator()).print(node.getSimpleSelector());
        return super.visit(node);
    }

    @Override
    public boolean visit(SimpleNode node) {
        print(node.getValue());
        return super.visit(node);
    }

    @Override
    public boolean visit(SingleLineCommentNode node) {
        print('\n'); // C-style single line comments are not part of the CSS spec, so don't print them
        return super.visit(node);
    }

    @Override
    public boolean enter(VariableDefinitionNode node) {
        print(node.getName()).print(": ");
        return super.enter(node);
    }

    @Override
    public boolean visit(VariableDefinitionNode node) {
        print(";");
        if (node.getParent().getChildren().size() > 1 && NodeTreeUtils.parentHasNext(node)) {
            print(' ');
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(VariableReferenceNode node) {
        print(node.getValue());
        return super.visit(node);
    }

    // Printing methods

    private ParsedPrinter print(String s) {
        _sb.append(s);
        _lastPrintedIndent = false;
        return this;
    }

    private ParsedPrinter print(Character c) {
        _sb.append(c);
        _lastPrintedIndent = false;
        return this;
    }

    private ParsedPrinter printIndent() {
        for (int i = 0; i < _indent; i++) {
            _sb.append(' ');
        }
        _lastPrintedIndent = true;
        return this;
    }

    private ParsedPrinter printLine() {
        _sb.append('\n');
        _lastPrintedIndent = false;
        return this;
    }

    private ParsedPrinter addIndent() {
        _indent += INDENT_STEP;
        return this;
    }

    private ParsedPrinter removeIndent() {
        _indent -= INDENT_STEP;
        return this;
    }

    private ParsedPrinter deleteIndent() {
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
