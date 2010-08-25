package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ParametersNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.node.VariableDefinitionNode;

/**
 * Visitors that wish to be notified when an {@link com.bazaarvoice.jless.ast.node.Node#add}
 * of some node type occurs implement this interface. {@link com.bazaarvoice.jless.ast.node.InternalNode#setAdditionVisitor}
 * is used to register such a visitor with the node.
 */
public interface NodeAdditionVisitor {

    // Base visitor methods
    // These eliminate the need for methods that no visitor uses

    /**
     * Called when a child node is about to be added.
     * @return Determines whether the node should be added.
     */
    boolean add(Node node);

    // Concrete visitor methods

    boolean add(ParametersNode node);

    boolean add(RuleSetNode node);

    boolean add(ScopeNode node);

    boolean add(SelectorNode node);

    boolean add(VariableDefinitionNode node);
}
