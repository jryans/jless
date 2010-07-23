package com.bazaarvoice.jless.parser;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

/**
 * Transcribed from the <a href="http://github.com/cloudhead/less/blob/master/lib/less/engine/grammar">LESS Treetop grammar</a>
 * by Alexis Sellier into Parboiled.
 *
 * @author J. Ryan Stinnett
 */
public class Parser extends BaseParser<Object> {

    // TODO: Remove Lower, Use Ident, etc.

    @SuppressSubnodes
    public Rule Primary() {
        return ZeroOrMore(FirstOf(/*Import(), */Declaration(), RuleSet()/*, Mixin()*/, Comment()));
    }

    Rule Comment() {
        return FirstOf(MultipleLineComment(), SingleLineComment());
    }

    /**
     * Ws0 '//' (!'\n' .)* '\n' Ws0
     */
    Rule SingleLineComment() {
        return Sequence(Ws0(), "//", ZeroOrMore(Sequence(TestNot('\n'), Any())), '\n', Ws0());
    }

    /**
     * Ws0 '/*' (!'*\/' .)* '*\/' Ws0
     */
    Rule MultipleLineComment() {
        return Sequence(Ws0(), "/*", ZeroOrMore(Sequence(TestNot("*/"), Any())), "*/", Ws0());
    }

    // ********** CSS Rule Sets **********

    /**
     * Selectors '{' Ws0 Primary Ws0 '}' Sp0 ';'? Ws0
     *
     * TODO: What is hide for? Add mixin.
     *
     * Ex: div, .class, body > p {...}
     */
    Rule RuleSet() {
        return Sequence(Selectors(), '{', Ws0(), Primary(), Ws0(), '}', Sp0(), Optional(';'), Ws0());
    }

    // ********** CSS Selectors **********

    /**
     * Ws0 Selector (Sp0 ',' Ws0 Selector)* Ws0
     */
    Rule Selectors() {
        return Sequence(Ws0(), Selector(), ZeroOrMore(Sequence(Sp0(), ',', Ws0(), Selector())), Ws0());
    }

    /**
     * (Sp0 Select Element Sp0)+
     *
     * Ex: div > p a { ... }
     */
    Rule Selector() {
        return OneOrMore(Sequence(Sp0(), Select(), Element(), Sp0()));
    }

    /**
     * (
     *     (Class / ID / Tag / Ident)
     *     Attribute*
     *     (
     *         '(' Alpha+ ')' / '(' (PseudoExp / Selector / Digit1) ')'
     *     )?
     * )+
     * / Attribute+ / '@media' / '@font-face'
     *
     * Ex: div / .class / #id / input[type="text"] / lang(fr)
     */
    Rule Element() {
        return FirstOf(
                OneOrMore(Sequence(
                        FirstOf(Class(), ID(), Tag(), Ident()),
                        ZeroOrMore(Attribute()),
                        Optional(FirstOf(
                                Sequence('(', OneOrMore(Alpha()), ')'),
                                Sequence('(', FirstOf(/*PseudoExp(), */Selector(), Digit1()), ')')
                        ))
                )),
                OneOrMore(Attribute()),
                "@media",
                "@font-face"
        );
    }

    /**
     * (Sp0 [+>~] Sp0 / '::' / Sp0 ':' / Sp1)?
     */
    Rule Select() {
        return Optional(FirstOf(
                Sequence(Sp0(), CharSet("+>~"), Sp0()),
                "::",
                Sequence(Sp0(), ':'),
                Sp1()
        ));
    }

    /**
     * '*'? '-'? [-_Alpha] [-_Alphanumeric]*
     */
    Rule Ident() {
        return Sequence(
                Optional('*'),
                Optional('-'),
                FirstOf(CharSet("-_"), Alpha()),
                ZeroOrMore(FirstOf(CharSet("-_"), Alphanumeric()))
        );
    }

    // ********** Variables & Expressions **********

    /**
     * Ws0 (Ident / Variable) Sp0 ':' Ws0 Expressions (Ws0 ',' Ws0 Expressions)* Sp0 (';' / Ws0 &'}') Ws0
     * / Ws0 Ident Sp0 ':' Sp0 ';' Ws0
     *
     * Ex: @my-var: 12px; height: 100%;
     */
    Rule Declaration() {
        return FirstOf(
                Sequence(
                        Ws0(),
                        /*FirstOf(*/Ident()/*, Variable())*/,
                        Sp0(),
                        ':',
                        Ws0(),
                        Expressions(),
                        ZeroOrMore(Sequence(Ws0(), ',', Ws0(), Expressions())),
                        Sp0(),
                        FirstOf(';', Sequence(Ws0(), Test('}'))),
                        Ws0()
                ),
                Sequence(Ws0(), Ident(), Sp0(), ':', Sp0(), ';', Ws0())
        );
    }

    /**
     * TODO: More than catch-all rule
     */
    Rule Expressions() {
        return OneOrMore(FirstOf(CharSet("-_.&*/=:,+? []()#%"), Alphanumeric()));
    }

    // ********** HTML Entities **********

    /**
     * '.' [_Alpha] [-_Alphanumeric]*
     */
    Rule Class() {
        return Sequence(
                '.',
                FirstOf('-', Alpha()),
                ZeroOrMore(FirstOf(CharSet("-_"), Alphanumeric()))
        );
    }

    /**
     * '#' [_Alpha] [-_Alphanumeric]*
     */
    Rule ID() {
        return Sequence(
                '#',
                FirstOf('-', Alpha()),
                ZeroOrMore(FirstOf(CharSet("-_"), Alphanumeric()))
        );
    }

    /**
     * Alpha [-Alpha]* Digit? / '*'
     */
    Rule Tag() {
        return FirstOf(
                Sequence(
                        Alpha(),
                        ZeroOrMore(FirstOf('-', Alpha())),
                        Optional(Digit())
                ),
                '*'
        );
    }

    /**
     * '[' AttributeName [|~*$^]? '=' (String / [-_Alphanumeric]+) ']' / '[' (AttributeName / String) ']'
     *
     * Ex: [type="text"]
     */
    Rule Attribute() {
        return FirstOf(
                Sequence(
                        '[',
                        AttributeName(),
                        Optional(CharSet("|~*$^")),
                        '=',
                        FirstOf(String(), OneOrMore(FirstOf(CharSet("-_"), Alphanumeric())))
                ),
                Sequence('[', FirstOf(AttributeName(), String()), ']')
        );
    }

    /**
     * Alpha [-Alpha]* Digit? / '*' (This may not be needed here)
     */
    Rule AttributeName() {
        return Tag();
    }

    // ********** Functions & Arguments **********

    /**
     * [-_Alpha]+ Arguments
     */
    /*Rule Function() {
        return Sequence(
                OneOrMore(FirstOf(CharSet("-_"), Alpha())),
                Arguments()
        );
    }*/

    /**
     * '(' Sp0 Expressions Sp0 (',' Sp0 Expressions Sp0)* ')' / '(' Sp0 ')'
     */

    // ********** Entities **********

    // TODO: Check use of !Nd

    /**
     * Any whitespace delimited token (??)
     */
    Rule Entity() {
        return FirstOf(/*URL(), AlphaFilter(), Function(), Accessor(), */Keyword(), /*Variable(), */Literal(), Font());
    }

    /**
     * Alpha [-Alphanumeric]* !Nd / String
     */
    Rule Font() {
        return FirstOf(
                Sequence(Alpha(), ZeroOrMore(FirstOf('-', Alphanumeric())), TestNot(Nd())),
                String()
        );
    }

    /**
     * Tokens that don't need to evaluated
     */
    Rule Literal() {
        return FirstOf(Color(), MultiDimension(), Dimension(), String());
    }

    /**
     * [-Alpha]+ !Nd
     *
     * Ex: blue, small, normal
     */
    Rule Keyword() {
        return Sequence(OneOrMore(FirstOf('-', Alpha())), TestNot(Nd()));
    }

    /**
     * Ex: 'hello' / "hello"
     */
    Rule String() {
        return FirstOf(
                Sequence('\'', ZeroOrMore(Sequence(TestNot('\''), Any())), '\''),
                Sequence('"', ZeroOrMore(Sequence(TestNot('"'), Any())), '"')
        );
    }

    /**
     * Some CSS properties allow multiple dimensions separated by '/'
     *
     * (Dimension / [-a-z]+) '/' Dimension
     */
    Rule MultiDimension() {
        return Sequence(
                FirstOf(
                        Dimension(),
                        OneOrMore(FirstOf('-', this.Alpha()))
                ),
                '/',
                Dimension()
        );
    }

    /**
     * Number Unit
     */
    Rule Dimension() {
        return Sequence(Number(), Unit());
    }

    /**
     * '-'? Digit* '.' Digit+ / '-'? Digit+
     */
    Rule Number() {
        return FirstOf(
                Sequence(Optional('-'), Digit0(), '.', Digit1()),
                Sequence(Optional('-'), Digit1())
        );
    }

    /**
     * ('px' / 'em' / 'pc' / '%' / 'ex' / 'in' / 'deg' / 's' / 'pt' / 'cm' / 'mm')?
     */
    Rule Unit() {
        return Optional(FirstOf("px", "em", "pc", '%', "ex", "in", "deg", 's', "pt", "cm", "mm"));
    }

    /**
     * '#' RGB / (('hsl' / 'rgb') 'a'?) Arguments
     */
    Rule Color() {
        return /*FirstOf(*/
                Sequence('#', RGB())/*,
                Sequence(FirstOf("hsl", "rgb"), Optional('a'), Arguments())
        )*/;
    }

    /**
     * Ex: 0099dd / 09d
     */
    Rule RGB() {
        return FirstOf(
                Sequence(Hex(), Hex(), Hex(), Hex(), Hex(), Hex()),
                Sequence(Hex(), Hex(), Hex())
        );
    }

    // ********** Characters & Simple Character Groups **********

    Rule Alphanumeric() {
        return FirstOf(Alpha(), Digit());
    }

    Rule Alpha() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
    }

    Rule Digit0() {
        return ZeroOrMore(Digit());
    }

    Rule Digit1() {
        return OneOrMore(Digit());
    }

    Rule Digit() {
        return CharRange('0', '9');
    }

    Rule Hex() {
        return FirstOf(CharRange('a', 'f'), CharRange('A', 'F'), Digit());
    }

    Rule Sp0() {
        return ZeroOrMore(' ');
    }

    Rule Sp1() {
        return OneOrMore(' ');
    }

    Rule Ws0() {
        return ZeroOrMore(Whitespace());
    }

    Rule Ws1() {
        return OneOrMore(Whitespace());
    }

    Rule Nd() {
        return Sequence(TestNot(Delimiter()), Any());
    }

    Rule Whitespace() {
        return CharSet(" \n");
    }

    Rule Delimiter() {
        return CharSet(" ;,!})\n");
    }
}
