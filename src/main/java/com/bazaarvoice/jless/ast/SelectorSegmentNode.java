package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class SelectorSegmentNode extends Node {

    private String _combinator;
    private String _simpleSelector;

    public SelectorSegmentNode(String combinator) {
        _combinator = combinator;
    }

    public SelectorSegmentNode(String combinator, String simpleSelector) {
        _combinator = combinator;
        _simpleSelector = simpleSelector;
    }

    public String getCombinator() {
        return _combinator;
    }

    public boolean setCombinator(String combinator) {
        _combinator = combinator;
        return true;
    }

    public String getSimpleSelector() {
        return _simpleSelector;
    }

    public boolean setSimpleSelector(String simpleSelector) {
        _simpleSelector = simpleSelector;
        return true;
    }

    @Override
    public boolean accept(NodeVisitor visitor) {
        if (visitor.visitEnter(this)) {
            for (Node child : getChildren()) {
                if (!child.accept(visitor)) {
                    break;
                }
            }
        }

        return visitor.visit(this);
    }
}
