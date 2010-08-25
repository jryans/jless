package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class SimpleNode extends LeafNode {

    private String _value;

    public SimpleNode(String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    @Override
    public boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    public boolean filter(NodeNavigationVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean traverse(NodeNavigationVisitor visitor) {
        return !isVisible() || visitor.visit(this);
    }
}
