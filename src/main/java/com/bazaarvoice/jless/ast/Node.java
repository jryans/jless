package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;
import org.parboiled.trees.MutableTreeNodeImpl;
import org.parboiled.trees.TreeUtils;

public abstract class Node extends MutableTreeNodeImpl<Node> {

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

    public boolean accept(NodeVisitor visitor) {
        if (visitor.visitEnter(this)) {
            for (Node child : getChildren()) {
                if (!child.accept(visitor)) {
                    break;
                }
            }
        }

        return visitor.visit(this);
    }
}