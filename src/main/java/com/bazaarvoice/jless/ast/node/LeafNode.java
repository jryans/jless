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

    @Override
    public boolean filter(NodeNavigationVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean traverse(NodeNavigationVisitor visitor) {
        return !isVisible() || visitor.visit(this);
    }
}
