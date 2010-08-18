package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;

public class SimpleNode extends LeafNode {

    private String _value;

    public SimpleNode(String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    @Override
    public boolean traverse(NodeTraversalVisitor visitor) {
        return visitor.visit(this);
    }
}
