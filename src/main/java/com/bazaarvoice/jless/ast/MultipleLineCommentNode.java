package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

import java.util.ListIterator;

public class MultipleLineCommentNode extends SimpleNode {

    public MultipleLineCommentNode(String text) {
        super(text);
    }

    @Override
    public boolean accept(NodeVisitor visitor) {
        if (visitor.visitEnter(this)) {
            for (ListIterator<Node> it = newChildIterator(); it.hasNext();) {
                Node child = it.next();
                if (!child.accept(visitor)) {
                    break;
                }
            }
        }

        return visitor.visit(this);
    }
}
