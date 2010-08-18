package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class LeafNode extends Node {

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
    public boolean accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }
}
