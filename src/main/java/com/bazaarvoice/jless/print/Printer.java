package com.bazaarvoice.jless.print;

import com.bazaarvoice.jless.ast.Node;
import org.parboiled.google.base.Preconditions;

public class Printer {

    public enum Optimization {
        NONE,
        LESS_RUBY,
        INFINITE // This is not selectable as an actual value
    }

    private StringBuilder _sb = new StringBuilder();
    private Optimization _optimization;

    public Printer() {
        _optimization = Optimization.NONE;
    }

    public Printer(Optimization optimization) {
        Preconditions.checkArgument(!optimization.equals(Optimization.INFINITE), "The optimization level cannot be set to infinite.");
        _optimization = optimization;
    }

    public <T> void printChildren(Node parent) {
        for (Node child : parent.getChildren()) {
            child.print(this);
        }
    }

    public Printer append(String s) {
        return append(s, Optimization.INFINITE);
    }

    public Printer append(String s, Optimization optionalAt) {
        if (_optimization.compareTo(optionalAt) < 0) {
            _sb.append(s);
        }
        return this;
    }
}
