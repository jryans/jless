package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class MultipleLineCommentNode extends SimpleNode {

    public MultipleLineCommentNode(String text) {
        super(text);
    }

    @Override
    protected boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    protected boolean visit(NodeNavigationVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    protected boolean visitInvisible(NodeNavigationVisitor visitor) {
        return visitor.visitInvisible(this);
    }
}
