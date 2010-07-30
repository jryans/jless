package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class CatchAllNode extends SimpleNode {

    public CatchAllNode(String text) {
        super(text);
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
