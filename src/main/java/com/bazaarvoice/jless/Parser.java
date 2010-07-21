package com.bazaarvoice.jless;

import org.parboiled.BaseParser;
import org.parboiled.Rule;

public class Parser extends BaseParser<Object> {

    public Rule Document() {
        
    }

    // ********** Entity **********

    // ********** Common **********

    private Rule Sp0() {
        return ZeroOrMore(' ');
    }

    private Rule Sp1() {
        return OneOrMore(' ');
    }

    private Rule Ws0() {
        return ZeroOrMore(WhitespaceChar());
    }

    private Rule Ws1() {
        return OneOrMore(WhitespaceChar());
    }

    private Rule Nd() {
        return Sequence(TestNot(DelimeterChar()), Any());
    }

    private Rule WhitespaceChar() {
        return CharSet(" \n");
    }

    private Rule DelimeterChar() {
        return CharSet(" ;,!})\n");
    }

}
