package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class SimpleNode extends Node {

    private String _value;

    public SimpleNode(String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
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
