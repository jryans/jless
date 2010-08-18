package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.VariableDefinitionNode;

/**
 *
 */
public class TranslatedPrinter extends ParsedPrinter {

    /**
     * Hide variable name and value
     */
    @Override
    public boolean enter(VariableDefinitionNode node) {
        return false;
    }

    /**
     * Hide variable definition terminator
     */
    @Override
    public boolean visit(VariableDefinitionNode node) {
        return true;
    }
}
