package com.bazaarvoice.jless.ast.visitor;

/**
 * Defaults to visiting every node.
 */
public class InclusiveNodeVisitor extends DefaultNodeVisitor {

    private static final InclusiveNodeVisitor SINGLETON = new InclusiveNodeVisitor();

    public static InclusiveNodeVisitor getInstance() {
        return SINGLETON;
    }

    @Override
    protected final boolean defaultValue() {
        return true;
    }
}
