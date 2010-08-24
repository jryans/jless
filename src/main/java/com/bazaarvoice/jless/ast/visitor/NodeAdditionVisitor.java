package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ParametersNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.VariableDefinitionNode;

/**
 *
 */
public interface NodeAdditionVisitor {

    // Base visitor methods
    // These eliminate the need for methods that no visitor uses

    boolean add(Node node);

    // Concrete visitor methods

    boolean add(ParametersNode node);

    boolean add(RuleSetNode node);

    boolean add(ScopeNode node);

    boolean add(VariableDefinitionNode node);
}
