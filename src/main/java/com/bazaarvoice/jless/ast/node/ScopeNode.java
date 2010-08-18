package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.util.MutableTreeUtils;
import com.bazaarvoice.jless.ast.visitor.InclusiveNodeVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

public class ScopeNode extends InternalNode {

    private final Map<String, ExpressionGroupNode> _variableMap = new HashMap<String, ExpressionGroupNode>();

    public ScopeNode() {
        super();

        // Store variable definitions in a map by name
        _additionVisitor = new InclusiveNodeVisitor() {
            @Override
            public boolean add(VariableDefinitionNode node) {
                _variableMap.put(node.getName(), MutableTreeUtils.getFirstChild(node, ExpressionGroupNode.class));

                return super.add(node);
            }
        };
    }

/*
    public ExpressionGroupNode resolveVariable(String name) {
        // Check this scope first
        ExpressionGroupNode value = _variableMap.get(name);
        if (value != null) {
            return value;
        }

        // Check the parent scope
        value = MutableTreeUtils.getParentScope(this).resolveVariable(name);
        if (value != null) {
            return value;
        }

        // Record error location
        throw new UndefinedVariableException(name);
    }
*/

    public ExpressionGroupNode resolveVariable(String name) {
        // Check this scope first
        return _variableMap.get(name);
    }

    @Override
    public boolean traverse(NodeTraversalVisitor visitor) {
        if (visitor.enter(this)) {
            ListIterator<Node> it = pushChildIterator();
            while (it.hasNext()) {
                Node child = it.next();
                if (!child.traverse(visitor)) {
                    break;
                }
            }
            popChildIterator();
        }

        return visitor.visit(this);
    }
}
