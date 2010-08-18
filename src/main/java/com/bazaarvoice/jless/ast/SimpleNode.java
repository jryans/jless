package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class SimpleNode extends LeafNode {

    private String _value;

    public SimpleNode(String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    @Override
    public boolean accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }
}
