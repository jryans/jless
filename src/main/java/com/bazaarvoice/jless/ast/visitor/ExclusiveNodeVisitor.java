package com.bazaarvoice.jless.ast.visitor;

/**
 * Defaults to visiting no nodes.
 */
public class ExclusiveNodeVisitor extends DefaultNodeVisitor {

    private static final ExclusiveNodeVisitor SINGLETON = new ExclusiveNodeVisitor();

    public static ExclusiveNodeVisitor getInstance() {
        return SINGLETON;
    }

    @Override
    protected boolean defaultValue() {
        return false;
    }
}
