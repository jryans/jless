package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

import java.util.Collections;
import java.util.List;

/**
 * Super class for all leaf nodes of the tree.
 */
public abstract class LeafNode extends Node {

    @Override
    public boolean addChild(Node child) {
        throw new UnsupportedOperationException("Leaf nodes can't contain children.");
    }

    @Override
    public void addChild(int index, Node child) {
        throw new UnsupportedOperationException("Leaf nodes can't contain children.");
    }

    @Override
    public void setChild(int index, Node child) {
        throw new UnsupportedOperationException("Leaf nodes can't contain children.");
    }

    @Override
    public Node removeChild(int index) {
        throw new UnsupportedOperationException("Leaf nodes can't contain children.");
    }

    @Override
    public List<Node> getChildren() {
        return Collections.emptyList();
    }

    protected abstract boolean visit(NodeNavigationVisitor visit);

    @Override
    public final boolean filter(NodeNavigationVisitor visitor) {
        return visit(visitor);
    }

    @Override
    public final boolean traverse(NodeNavigationVisitor visitor) {
        return !isVisible() || visit(visitor);
    }
}
