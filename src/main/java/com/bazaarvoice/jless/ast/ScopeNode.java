package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

public class ScopeNode extends InternalNode {

    private Map<String, ExpressionGroupNode> _variables = new HashMap<String, ExpressionGroupNode>();

    @Override
    public boolean accept(NodeVisitor visitor) {
        if (visitor.visitEnter(this)) {
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
