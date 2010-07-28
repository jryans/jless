package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;

public class SelectorSegmentNode extends Node {

    private String _select;
    private String _element;

    public SelectorSegmentNode() {
    }

    public SelectorSegmentNode(String select) {
        _select = select;
    }

    public SelectorSegmentNode(String select, String element) {
        _select = select;
        _element = element;
    }

    public String getSelect() {
        return _select;
    }

    public boolean setSelect(String select) {
        _select = select;
        return true;
    }

    public String getElement() {
        return _element;
    }

    public boolean setElement(String element) {
        _element = element;
        return true;
    }

    @Override
    public boolean accept(NodeVisitor visitor) {
        if (visitor.visitEnter(this)) {
            for (Node child : getChildren()) {
                if (!child.accept(visitor)) {
                    break;
                }
            }
        }

        return visitor.visit(this);
    }
}
