package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;
import org.parboiled.trees.MutableTreeNode;

/**
 *
 */
public abstract class Node implements MutableTreeNode<Node>, Cloneable {
    private InternalNode _parent;

    @Override
    public InternalNode getParent() {
        return _parent;
    }

    public void setParent(InternalNode parent) {
        _parent = parent;
    }

    /**
     * This simplifies parsing rules by allowing you to always create nodes and call addChild(),
     * and then the concrete node can report whether it actually contains data that should be
     * added to the AST.
     */
    protected boolean hasData() {
        return true;
    }

    public abstract boolean addChild(Node child);

    public abstract boolean traverse(NodeTraversalVisitor visitor);

    public boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @SuppressWarnings ({"unchecked", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    public Node clone() {
        Node node;
        try {
            // Clone any primitive fields
            node = (Node) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Object is marked Cloneable, but super.clone() failed.");
        }

        // Reset internal state
        node._parent = null;

        return node;
    }
}
