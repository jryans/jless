package com.bazaarvoice.jless.ast;

public class SelectorNode extends Node {

    private String _select;
    private String _element;

    public SelectorNode(String select) {
        _select = select;
    }

    public SelectorNode(String select, String element) {
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
}
