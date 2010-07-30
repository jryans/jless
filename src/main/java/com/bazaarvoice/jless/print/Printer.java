package com.bazaarvoice.jless.print;

import com.bazaarvoice.jless.ast.ExpressionNode;
import com.bazaarvoice.jless.ast.ExpressionsNode;
import com.bazaarvoice.jless.ast.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.PropertyNode;
import com.bazaarvoice.jless.ast.RuleSetNode;
import com.bazaarvoice.jless.ast.ScopeNode;
import com.bazaarvoice.jless.ast.SelectorNode;
import com.bazaarvoice.jless.ast.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.SelectorsNode;
import com.bazaarvoice.jless.ast.SimpleNode;
import com.bazaarvoice.jless.ast.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.visitor.NodeVisitor;
import org.parboiled.google.base.Preconditions;
import org.parboiled.trees.GraphUtils;

public class Printer extends NodeVisitor {

    private StringBuilder _sb = new StringBuilder();
    private Optimization _optimization;

    public Printer() {
        _optimization = Optimization.NONE;
    }

    public Printer(Optimization optimization) {
        Preconditions.checkArgument(!optimization.equals(Optimization.INFINITE), "The optimization level cannot be set to infinite.");
        _optimization = optimization;
    }

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
        return true;
    }

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        if (isIncluded(Optimization.LESS_RUBY)) {
            print("/*").print(node.getValue()).print("*/");
        }
        return true;
    }

    @Override
    public boolean visitEnter(PropertyNode node) {
        print(' ').print(node.getName()).print(": ");
        return true;
    }

    @Override
    public boolean visit(PropertyNode node) {
        print(";");
        return true;
    }

    @Override
    public boolean visit(RuleSetNode node) {
        return true;
    }

    @Override
    public boolean visitEnter(ScopeNode node) {
        if (node.getParent() != null) {
            print(" {");
        }
        return true;
    }

    @Override
    public boolean visit(ScopeNode node) {
        if (node.getParent() != null) {
            print(" }\n");
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
    public boolean visit(SelectorsNode node) {
        return true;
    }

    @Override
    public boolean visit(SelectorSegmentNode node) {
        print(node.getSelect()).print(node.getElement());
        if (GraphUtils.getLastChild(node.getParent()) != node) {
            print(' ');
        }
        return true;
    }

    @Override
    public boolean visit(SimpleNode node) {
        print(node.getValue());
        return true;
    }

    @Override
    public boolean visit(SingleLineCommentNode node) {
        if (isIncluded(Optimization.LESS_RUBY)) {
            print("//").print(node.getValue()).print('\n');
        }
        return true;
    }

    // Printing methods

    private Printer print(String s) {
        return print(s, Optimization.INFINITE);
    }

    private Printer print(String s, Optimization optionalAt) {
        if (isIncluded(optionalAt)) {
            _sb.append(s);
        }
        return this;
    }

    private Printer print(Character c) {
        return print(c, Optimization.INFINITE);
    }

    private Printer print(Character c, Optimization optionalAt) {
        if (isIncluded(optionalAt)) {
            _sb.append(c);
        }
        return this;
    }

    private boolean isIncluded(Optimization optionalAt) {
        return _optimization.compareTo(optionalAt) < 0;
    }

    @Override
    public String toString() {
        return _sb.toString();
    }
}
