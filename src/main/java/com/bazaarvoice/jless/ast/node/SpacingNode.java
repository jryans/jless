package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;

public class SpacingNode extends SimpleNode {

    public SpacingNode(String text) {
        super(text);
    }

    @Override
    protected boolean hasData() {
        return getValue().length() > 0;
    }

    @Override
    public boolean accept(NodeTraversalVisitor visitor) {
        return visitor.visit(this);
    }
}
