package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class SelectorsNode extends Node {

    public SelectorsNode(Node child) {
        super(child);
    }

    @Override
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
