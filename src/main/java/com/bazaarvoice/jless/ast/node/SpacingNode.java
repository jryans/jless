package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class SpacingNode extends SimpleNode {

    public SpacingNode(String text) {
        super(text);
    }

    @Override
    protected boolean hasData() {
        return getValue().length() > 0;
    }

    @Override
    protected boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    protected boolean visit(NodeNavigationVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    protected boolean visitInvisible(NodeNavigationVisitor visitor) {
        return visitor.visitInvisible(this);
    }
}
