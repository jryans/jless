package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

// TODO: Combine into BTN
public abstract class Node extends BaseTreeNode<Node> {

    public Node() {
    }

    public Node(Node child) {
        this();
        addChild(child);
    }

    public boolean addChild(Node child) {
        if (child != null && child.hasData()) {
            addChild(getChildren().size(), child);
        }
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

    public abstract boolean accept(NodeVisitor visitor);
}   