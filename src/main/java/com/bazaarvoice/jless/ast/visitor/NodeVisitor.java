package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.ast.SelectorNode;
import com.bazaarvoice.jless.ast.SingleLineCommentNode;

public abstract class NodeVisitor {

    public boolean visitEnter(Node node) {
        throw new IllegalArgumentException("No visitEnter method for class " + node.getClass().getSimpleName());
    }

    public boolean visit(Node node) {
        throw new IllegalArgumentException("No visit method for class " + node.getClass().getSimpleName());
    }

    public abstract boolean visit(MultipleLineCommentNode node);

    public abstract boolean visit(SelectorNode node);

    public abstract boolean visit(SingleLineCommentNode node);
}
