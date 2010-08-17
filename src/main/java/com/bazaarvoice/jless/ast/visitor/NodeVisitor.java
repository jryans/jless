package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.ExpressionGroupNode;
import com.bazaarvoice.jless.ast.ExpressionNode;
import com.bazaarvoice.jless.ast.ExpressionsNode;
import com.bazaarvoice.jless.ast.LineBreakNode;
import com.bazaarvoice.jless.ast.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.PropertyNode;
import com.bazaarvoice.jless.ast.RuleSetNode;
import com.bazaarvoice.jless.ast.ScopeNode;
import com.bazaarvoice.jless.ast.SelectorGroupNode;
import com.bazaarvoice.jless.ast.SelectorNode;
import com.bazaarvoice.jless.ast.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.SimpleNode;
import com.bazaarvoice.jless.ast.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.VariableDefinitionNode;

/**
 *
 */
public interface NodeVisitor {

    // Base visit methods
    // These eliminate the need for visit methods that no visitor uses

/*
    boolean visitEnter(Node node);

    boolean visit(Node node);
*/

    // Concrete visit methods

    boolean visitEnter(ExpressionGroupNode node);

    boolean visit(ExpressionGroupNode node);

    boolean visit(ExpressionNode node);

    boolean visit(ExpressionsNode node);

    boolean visit(LineBreakNode node);

    boolean visit(MultipleLineCommentNode node);

    boolean visitEnter(PropertyNode node);

    boolean visit(PropertyNode node);

    boolean visitEnter(RuleSetNode node);

    boolean visit(RuleSetNode node);

    boolean visitEnter(ScopeNode node);

    boolean visit(ScopeNode node);

    boolean visitEnter(SelectorNode node);

    boolean visit(SelectorNode node);

    boolean visitEnter(SelectorGroupNode node);

    boolean visit(SelectorGroupNode node);

    boolean visit(SelectorSegmentNode node);

    boolean visit(SimpleNode node);

    boolean visit(SingleLineCommentNode node);

    boolean visitEnter(VariableDefinitionNode node);

    boolean visit(VariableDefinitionNode node);
}
