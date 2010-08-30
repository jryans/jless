package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class PropertyNode extends InternalNode {

    private String _name;

    public PropertyNode(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    @Override
    protected boolean add(NodeAdditionVisitor visitor) {
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

    @Override
    protected boolean visitInvisible(NodeNavigationVisitor visitor) {
        return visitor.visitInvisible(this);
    }
}
