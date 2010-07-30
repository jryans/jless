package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class ExpressionGroupNode extends Node {

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
