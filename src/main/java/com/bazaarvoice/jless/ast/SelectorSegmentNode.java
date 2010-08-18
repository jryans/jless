package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class SelectorSegmentNode extends LeafNode {

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
    public boolean accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }
}
