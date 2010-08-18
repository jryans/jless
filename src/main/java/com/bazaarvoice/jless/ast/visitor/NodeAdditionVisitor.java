package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.VariableDefinitionNode;

/**
 *
 */
public interface NodeAdditionVisitor {

    // Base visitor methods
    // These eliminate the need for methods that no visitor uses

    boolean add(Node node);

    // Concrete visitor methods

    boolean add(VariableDefinitionNode node);    
}
