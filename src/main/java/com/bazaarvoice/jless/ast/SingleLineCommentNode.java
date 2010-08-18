package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class SingleLineCommentNode extends SimpleNode {

    public SingleLineCommentNode(String text) {
        super(text);
    }

    @Override
    public boolean accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }
}
