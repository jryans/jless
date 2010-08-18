package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class SpacingNode extends SimpleNode {

    public SpacingNode(String text) {
        super(text);
    }

    @Override
    protected boolean hasData() {
        return getValue().length() > 0;
    }

    @Override
    public boolean accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }
}
