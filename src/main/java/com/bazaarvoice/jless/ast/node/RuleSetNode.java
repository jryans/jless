package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

import java.util.ListIterator;

public class RuleSetNode extends InternalNode {

    public RuleSetNode(Node child) {
        super(child);
    }

    @Override
    public boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    public boolean filter(NodeNavigationVisitor visitor) {
        if (visitor.enter(this)) {
            ListIterator<Node> it = pushChildIterator();
            while (it.hasNext()) {
                Node child = it.next();
                if (!child.filter(visitor)) {
                    it.remove();
                }
            }
            popChildIterator();
        }

        return visitor.visit(this);
    }

    @Override
    public boolean traverse(NodeNavigationVisitor visitor) {
        if (!isVisible()) {
            return true;
        }

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
