package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class ExpressionNode extends InternalNode {

    public ExpressionNode(Node child) {
        super(child);
    }

    @Override
    public boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    protected boolean enter(NodeNavigationVisitor visitor) {
        return visitor.enter(this);
    }

    @Override
    protected boolean exit(NodeNavigationVisitor visitor) {
        return visitor.exit(this);
    }
}
