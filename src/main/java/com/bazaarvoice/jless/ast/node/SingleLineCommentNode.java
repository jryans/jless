package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class SingleLineCommentNode extends SimpleNode {

    public SingleLineCommentNode(String text) {
        super(text);
    }

    @Override
    public boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    protected boolean visit(NodeNavigationVisitor visitor) {
        return visitor.visit(this);
    }
}
