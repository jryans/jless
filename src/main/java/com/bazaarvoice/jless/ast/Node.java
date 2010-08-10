package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;
import org.parboiled.trees.TreeUtils;

public abstract class Node extends BaseTreeNode<Node> {

    public Node() {
    }

    public Node(Node child) {
        this();
        addChild(child);
    }

    public boolean addChild(Node child) {
        if (child != null) {
            TreeUtils.addChild(this, child);
        }
        return true;
    }

    public abstract boolean accept(NodeVisitor visitor);
}