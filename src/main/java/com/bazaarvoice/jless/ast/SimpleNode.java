package com.bazaarvoice.jless.ast;

public class SimpleNode extends Node {

    private String _value;

    public SimpleNode(String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
    }
}
