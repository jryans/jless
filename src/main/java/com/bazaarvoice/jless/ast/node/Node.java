package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;
import com.bazaarvoice.jless.ast.visitor.Printer;
import org.parboiled.trees.MutableTreeNode;

public abstract class Node implements MutableTreeNode<Node>, Cloneable {
    private InternalNode _parent;
    private boolean _visible = true;

    @Override
    public InternalNode getParent() {
        return _parent;
    }

    public void setParent(InternalNode parent) {
        _parent = parent;
    }

    public boolean isVisible() {
        return _visible;
    }

    /**
     * Controls whether the node and its children will be visited by a traversal visitor.
     */
    public boolean setVisible(boolean visible) {
        _visible = visible;
        return true;
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

    public abstract boolean add(NodeAdditionVisitor visitor);

    protected abstract boolean enter(NodeNavigationVisitor visitor);

    protected abstract boolean visit(NodeNavigationVisitor visitor);

    public abstract boolean filter(NodeNavigationVisitor visitor);

    public abstract boolean traverse(NodeNavigationVisitor visitor);

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

    @Override
    public String toString() {
        Printer p = new Printer();
        traverse(p);
        return p.toString();
    }
}
