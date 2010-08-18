package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.InclusiveNodeVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

public class ScopeNode extends InternalNode {

    private final Map<String, ExpressionGroupNode> _variables = new HashMap<String, ExpressionGroupNode>();

    public ScopeNode() {
        super();
        _additionVisitor = new InclusiveNodeVisitor() {
            @Override
            public boolean add(VariableDefinitionNode node) {


                return super.add(node);
            }
        };
    }

    @Override
    public boolean accept(NodeTraversalVisitor visitor) {
        if (visitor.enter(this)) {
            ListIterator<Node> it = pushChildIterator();
            while (it.hasNext()) {
                Node child = it.next();
                if (!child.accept(visitor)) {
                    break;
                }
            }
            popChildIterator();
        }

        return visitor.visit(this);
    }
}
