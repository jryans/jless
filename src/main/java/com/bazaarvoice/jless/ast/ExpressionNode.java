package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class ExpressionNode extends Node {

    public ExpressionNode(Node child) {
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
