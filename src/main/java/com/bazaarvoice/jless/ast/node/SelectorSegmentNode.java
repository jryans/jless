package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class SelectorSegmentNode extends LeafNode {

    public static final String NO_COMBINATOR = "";
    public static final String DESCENDANT_COMBINATOR = " ";

    private String _combinator;
    private String _simpleSelector;
    private boolean _subElementSelector;

    public SelectorSegmentNode(String combinator) {
        _combinator = combinator;
    }

    public SelectorSegmentNode(String combinator, String simpleSelector) {
        _combinator = combinator;
        _simpleSelector = simpleSelector;
    }

    public String getCombinator() {
        return _combinator;
    }

    public boolean setCombinator(String combinator) {
        _combinator = combinator;
        return true;
    }

    public String getSimpleSelector() {
        return _simpleSelector;
    }

    public boolean setSimpleSelector(String simpleSelector) {
        _simpleSelector = simpleSelector;
        return true;
    }

    /**
     * Implies that the simple selector starts with some sub-element selection, such as an attribute or pseudo-class.
     */
    public boolean isSubElementSelector() {
        return _subElementSelector;
    }

    public boolean setSubElementSelector(boolean subElementSelector) {
        _subElementSelector = subElementSelector;
        return true;
    }

    @Override
    public boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    public boolean filter(NodeNavigationVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean traverse(NodeNavigationVisitor visitor) {
        return !isVisible() || visitor.visit(this);
    }
}
