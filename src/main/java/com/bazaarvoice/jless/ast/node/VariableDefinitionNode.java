package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class VariableDefinitionNode extends InternalNode {

    private String _name;

    public VariableDefinitionNode(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
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
