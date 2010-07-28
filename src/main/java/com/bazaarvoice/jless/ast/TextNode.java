package com.bazaarvoice.jless.ast;

public class TextNode extends Node {

    private String _text;

    public TextNode(String text) {
        _text = text;
    }

    public String getText() {
        return _text;
    }
}
