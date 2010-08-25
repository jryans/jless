package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.util.NodeTreeUtils;
import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;

public class VariableReferenceNode extends LeafNode {

    private String _name;

    public VariableReferenceNode(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    /**
     * Search up the scope tree to locate the variable's value. The parser has already verified that
     * the variable is defined.
     */
    public String getValue() {
        for (ScopeNode scope = NodeTreeUtils.getParentScope(this); scope != null; scope = NodeTreeUtils.getParentScope(scope)) {
            ExpressionGroupNode value = scope.getVariable(_name);
            if (value == null) {
                continue;
            }

            return value.toString();
        }

        throw new IllegalStateException("The variable " + _name + " could not be found in any parent scope.");
    }

    @Override
    public boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    public boolean filter(NodeTraversalVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean traverse(NodeTraversalVisitor visitor) {
        return !isVisible() || visitor.visit(this);
    }
}
