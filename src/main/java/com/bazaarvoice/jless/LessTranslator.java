package com.bazaarvoice.jless;

import com.bazaarvoice.jless.parser.Parser;
import org.parboiled.Parboiled;

public class LessTranslator {

    private Parser _parser;

    public LessTranslator() {
        _parser = Parboiled.createParser(Parser.class);
    }

    public Parser getParser() {
        return _parser;
    }
}
