package com.bazaarvoice.jless.print;

import com.bazaarvoice.jless.ast.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.SelectorNode;
import com.bazaarvoice.jless.ast.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.visitor.NodeVisitor;
import org.parboiled.google.base.Preconditions;

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

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        if (isIncluded(Optimization.LESS_RUBY)) {
            print("/*").print(node.getText()).print("*/");
        }
        return true;
    }

    @Override
    public boolean visit(SelectorNode node) {
        print(node.getSelect()).print(node.getElement());
        return true;
    }

    @Override
    public boolean visit(SingleLineCommentNode node) {
        if (isIncluded(Optimization.LESS_RUBY)) {
            print("//").print(node.getText()).print('\n');
        }
        return true;
    }

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
}
