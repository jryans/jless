package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.ExpressionNode;
import com.bazaarvoice.jless.ast.ExpressionsNode;
import com.bazaarvoice.jless.ast.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.ast.PropertyNode;
import com.bazaarvoice.jless.ast.RuleSetNode;
import com.bazaarvoice.jless.ast.ScopeNode;
import com.bazaarvoice.jless.ast.SelectorGroupNode;
import com.bazaarvoice.jless.ast.SelectorNode;
import com.bazaarvoice.jless.ast.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.SimpleNode;
import com.bazaarvoice.jless.ast.SingleLineCommentNode;

public abstract class NodeVisitor {

    public boolean visitEnter(Node node) {
//        throw new IllegalArgumentException("No visitEnter method for class " + node.getClass().getSimpleName());
        return true;
    }

    public boolean visit(Node node) {
        throw new IllegalArgumentException("No visit method for class " + node.getClass().getSimpleName());
    }

    public abstract boolean visit(ExpressionNode node);

    public abstract boolean visit(ExpressionsNode node);

    public abstract boolean visit(MultipleLineCommentNode node);

    public abstract boolean visitEnter(PropertyNode node);

    public abstract boolean visit(PropertyNode node);

    public abstract boolean visit(RuleSetNode node);

    public abstract boolean visitEnter(ScopeNode node);

    public abstract boolean visit(ScopeNode node);

    public abstract boolean visit(SelectorNode node);

    public abstract boolean visit(SelectorGroupNode node);

    public abstract boolean visit(SelectorSegmentNode node);

    public abstract boolean visit(SimpleNode node);

    public abstract boolean visit(SingleLineCommentNode node);
}
