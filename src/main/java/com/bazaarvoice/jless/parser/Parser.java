package com.bazaarvoice.jless.parser;

import com.bazaarvoice.jless.ast.ExpressionNode;
import com.bazaarvoice.jless.ast.ExpressionsNode;
import com.bazaarvoice.jless.ast.LineBreakNode;
import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.ast.PlaceholderNode;
import com.bazaarvoice.jless.ast.PropertyNode;
import com.bazaarvoice.jless.ast.RuleSetNode;
import com.bazaarvoice.jless.ast.ScopeNode;
import com.bazaarvoice.jless.ast.SelectorGroupNode;
import com.bazaarvoice.jless.ast.SelectorNode;
import com.bazaarvoice.jless.ast.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.SimpleNode;
import com.bazaarvoice.jless.ast.VariableDefinitionNode;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.MemoMismatches;
import org.parboiled.support.Var;

/**
 * Initially transcribed into Parboiled from the
 * <a href="http://github.com/cloudhead/less/blob/master/lib/less/engine/grammar">LESS Treetop grammar</a>
 * that was created by Alexis Sellier as part of the LESS Ruby implementation.
 *
 * From there, the parser has been modified to more closely support Parboiled style and a modified set of requirements.
 * Differences from the LESS Ruby parser include:
 * <ul>
 *   <li>Selector parsing was rewritten using the <a href="http://www.w3.org/TR/css3-selectors/#grammar">CSS 3 selector grammar</a></li> 
 *   <li>Numbers in attribute selectors must be quoted (as per the CSS spec)</li>
 *   <li>Case-sensitivity was removed</li>
 *   <li>Line breaks can be used in all places where spaces are accepted</li>
 * </ul>
 *
 * This parser attempts to track enough input whitespace and comments to cause the input and output files to have the same number of lines
 * (which is helpful when referencing styles via browser tools like Firebug). However, to avoid filling the AST with spacing nodes, a
 * simplifying assumption has been made:
 * <ul>
 *   <li>A rule set may not span multiple lines</li>
 * </ul>
 *
 * This list only notes changes in the <em>parsing</em> stage. See {@link com.bazaarvoice.jless.LessTranslator} for details on any changes
 * to the <em>translation</em> stage.
 *
 * TODO: Improve rule set line ending capture
 *
 * @see com.bazaarvoice.jless.LessTranslator
 */
public class Parser extends BaseParser<Node> {

    public Rule Document() {
        return Sequence(Scope(), EOI);
    }

    /**
     * This is the high-level rule at some scope (either the root document or within a rule set / mixin).
     * Future: Imports
     */
    public Rule Scope() {
        return Sequence(
                push(new ScopeNode()),
                ZeroOrMore(
//                        debug(getContext()),
                        FirstOf(
                                Declaration(),
                                RuleSet(),
                                MixinReference(),
                                Sequence(Sp1(), push(new LineBreakNode(match())))
                        ),
                        debug(getContext()),
                        peek(1).addChild(pop())//,
//                        debug(getContext())
                )
        );
    }

    Rule Comment() {
        return /*Sequence(debug(getContext()), */FirstOf(MultipleLineComment(), SingleLineComment())/*, debug(getContext()))*/;
    }

    /**
     * '//' (!'\n' .)* '\n' Ws0
     */
    Rule SingleLineComment() {
        return Sequence(
                "//", ZeroOrMore(TestNot('\n'), ANY),
                '\n', Ws0()
        );
    }

    /**
     * '/*' (!'*\/' .)* '*\/' Ws0
     */
    Rule MultipleLineComment() {
        return Sequence(
                "/*", ZeroOrMore(TestNot("*/"), ANY),
                "*/", Ws0()
        );
    }

    // ********** CSS Rule Sets & Mixins **********

    /**
     * (Selectors '{' Ws0 / Class Ws0 Parameters Ws0 '{' Ws0) Scope Ws0 '}' Ws0
     *
     * Ex: div, .class, body > p {...}
     *
     * Future: Hidden
     */
    Rule RuleSet() {
        return Sequence(
                FirstOf(
                        // Standard CSS rule set
                        Sequence(
                                SelectorGroup(), push(new RuleSetNode(pop())),
                                '{'
                        ),
                        // Mixin rule set, with possible arguments
                        Sequence(
                                ClassSelectorGroup(), push(new RuleSetNode(pop())), Ws0(),
                                Parameters(), Ws0(),
                                '{'
                        )
                ),
//                Ws0(), peek().addChild(new LineBreakNode(match())),
                Scope(), peek(1).addChild(pop()), Ws0(),
                '}', Ws0(), peek().addChild(new LineBreakNode(match()))
        );
    }

    /**
     * '(' Parameter Ws0 (',' Ws0 Parameter)* ')'
     */
    Rule Parameters() {
        return Sequence(
                '(', Parameter(), Ws0(),
                ZeroOrMore(
                        ',', Ws0(), Parameter()
                ),
                ')'
        );
    }

    /**
     * Variable Ws0 ':' Ws0 Expressions
     */
    Rule Parameter() {
        return Sequence(Variable(), Ws0(), ':', Ws0(), Expressions(), drop());
    }

    /**
     * SelectorGroup ';' Ws0
     * / Class Arguments Ws0 ';' Ws0
     * TODO: Capture whitespace?
     * TODO: Mixin name validation?
     */
    Rule MixinReference() {
        return FirstOf(
                // No arguments, reference an existing rule set's properties
                Sequence(SelectorGroup(), ';', Ws0(), push(new PlaceholderNode(pop()))),
                // Call a mixin, passing along some arguments
                Sequence(Class(), Arguments(), Ws0(), ';', Ws0(), push(new PlaceholderNode()))
        );
    }

    // ********** CSS Selectors **********

    /**
     * Selector Ws0 (',' Ws0 Selector)* Ws0
     */
    @MemoMismatches
    Rule SelectorGroup() {
        return Sequence(
                Selector(), push(new SelectorGroupNode(pop())), Ws0(),
                ZeroOrMore(
                        ',', Ws0(), peek().addChild(new LineBreakNode(match())),
                        Selector(), peek(1).addChild(pop())
                ),
                Ws0(), peek().addChild(new LineBreakNode(match()))
        );
    }

    /**
     * Special case rule that builds a selector for only a single class
     */
    Rule ClassSelectorGroup() {
        return Sequence(
                Class(),
                push(new SelectorGroupNode(new SelectorNode(new SelectorSegmentNode("", match()))))
        );
    }

    /**
     * SimpleSelector (Combinator SimpleSelector)*
     */
    Rule Selector() {
        Var<SelectorSegmentNode> selectorSegmentNode = new Var<SelectorSegmentNode>();
        return Sequence(
                push(new SelectorNode()),
                // First selector segment may have a combinator (with nested rule sets)
                Optional(SymbolCombinator()), selectorSegmentNode.set(new SelectorSegmentNode(match())),
                SimpleSelector(selectorSegmentNode), selectorSegmentNode.get().setSimpleSelector(match()),
                peek().addChild(selectorSegmentNode.getAndClear()),
//                debug(getContext()),
                // Additional selector segments must have a combinator
                ZeroOrMore(
                        Combinator(), selectorSegmentNode.set(new SelectorSegmentNode(match())),
                        SimpleSelector(selectorSegmentNode), selectorSegmentNode.get().setSimpleSelector(match()),
                        peek().addChild(selectorSegmentNode.getAndClear())
                )
//                debug(getContext())
        );
    }

    /**
     * Ws0 [+>~] Ws0 / Ws1
     */
    Rule Combinator() {
        return FirstOf(
                Sequence(Ws0(), SymbolCombinator()),
                DescendantCombinator()
        );
    }

    Rule SymbolCombinator() {
        return Sequence(AnyOf("+>~"), Ws0());
    }

    Rule DescendantCombinator() {
        return Ws1();
    }

    Rule SimpleSelector(Var<SelectorSegmentNode> selectorSegmentNode) {
        return FirstOf(
                Sequence(
//                        debug(getContext()),
                        FirstOf(ElementName(), UniversalHtml(), Universal()),
//                        debug(getContext()),
                        ZeroOrMore(FirstOf(Hash(), Class(), Attribute(), Negation(), Pseudo()))
//                        debug(getContext())
                ),
                OneOrMore(FirstOf(
                        Hash(), Class(),
                        Sequence(
                                FirstOf(Attribute(), Negation(), Pseudo()),
                                selectorSegmentNode.get().setSubElementSelector(true)
                        )
                )),
                "@media",
                "@font-face"
        );
    }

    Rule Attribute() {
        return Sequence(
                '[', Ws0(),
                Ident(), Ws0(),
                Optional(
                        Optional(AnyOf("~|^$*")),
                        '=', Ws0(),
                        FirstOf(Ident(), String()), Ws0()
                ),
                ']'
        );
    }

    Rule Pseudo() {
        return Sequence(':', Optional(':'), FirstOf(FunctionalPseudo(), Ident()));
    }

    Rule FunctionalPseudo() {
        return Sequence(Ident(), '(', Ws0(), PseudoExpression(), ')');
    }

    // TODO: Use Number in place of Digit
    Rule PseudoExpression() {
        return OneOrMore(FirstOf(AnyOf("+-"), Dimension(), Digit(), String(), Ident()), Ws0());
    }

    Rule Negation() {
        return Sequence(":not(", Ws0(), NegationArgument(), Ws0(), ')');
    }

    Rule NegationArgument() {
        return FirstOf(ElementName(), Universal(), Hash(), Class(), Attribute(), Pseudo());
    }

    // ********** Variables & Expressions **********

    /**
     * (Ident / Variable) Ws0 ':' Ws0 Expressions (Ws0 ',' Ws0 Expressions)* Sp0 (';' / Ws0 &'}') Ws0
     * / Ident Ws0 ':' Ws0 ';' Ws0
     *
     * Ex: @my-var: 12px; height: 100%;
     */
    Rule Declaration() {
        return FirstOf(
                Sequence(
                        FirstOf(
                                Sequence(PropertyName(), push(new PropertyNode(match()))),
                                Sequence(Variable(), push(new VariableDefinitionNode(match())))
                        ), Ws0(),
                        ':', Ws0(),
                        Expressions(), peek(1).addChild(pop()),
                        ZeroOrMore(
                                Ws0(), ',', Ws0(), Expressions(), peek(1).addChild(pop())
                        ),
                        Sp0(), FirstOf(';', Sequence(Ws0(), Test('}')))/*,
                        Ws0(), peek().addChild(new LineBreakNode(match()))*/
                ),
                // Empty rules are ignored (TODO: Remove?)
                Sequence(
                        Ident(), push(new PlaceholderNode()), Ws0(),
                        ':', Ws0(),
                        ';'/*, Ws0(), peek().addChild(new LineBreakNode(match()))*/
                )
        );
    }

    Rule Variable() {
        return Sequence('@', Ident());
    }

    /**
     * Expression (Operator Expression)+ Future: Operations
     * / Expression (Ws1 Expression)* Important?
     */
    Rule Expressions() {
        // Space-separated expressions
        return Sequence(
                Expression(), push(new ExpressionsNode(pop())),
//                        debug(getContext()),
                ZeroOrMore(Ws1(), Expression(), peek(1).addChild(pop())),
                Optional(Ws0(), Important(), peek(1).addChild(pop()))
        );
    }

    /**
     * '(' Ws0 Expressions Ws0 ')' Future: Operations
     * / Entity
     */
    Rule Expression() {
        return Sequence(Entity(), push(new ExpressionNode(pop())));
    }

    /**
     * '!' Ws0 'important'
     */
    Rule Important() {
        return Sequence('!', Ws0(), "important", push(new SimpleNode("!important")));
    }

    // ********** Browser Hack Workarounds **********

    Rule PropertyName() {
        return Sequence(Optional(AnyOf("*_")), Ident());
    }

    Rule UniversalHtml() {
        return Sequence(Universal(), "html");
    }

    // ********** CSS Entities **********

    Rule ElementName() {
        return Ident();
    }

    Rule Universal() {
        return Ch('*');
    }

    Rule Hash() {
        return Sequence('#', Name());
    }

    @MemoMismatches
    Rule Class() {
        return Sequence('.', Ident());
    }

    @MemoMismatches
    Rule Ident() {
        return Sequence(Optional('-'), NameStart(), ZeroOrMore(NameCharacter()));
    }

    Rule Name() {
        return OneOrMore(NameCharacter());
    }

    // ********** Functions & Arguments **********

    /**
     * [-_Alpha]+ Arguments
     */
    Rule Function() {
        return Sequence(
                OneOrMore(FirstOf(AnyOf("-_"), Alpha())),
                Arguments()
        );
    }

    /**
     * '(' Ws0 Expressions Ws0 (',' Ws0 Expressions Ws0)* ')' / '(' Ws0 ')'
     */
    Rule Arguments() {
        return FirstOf(
                Sequence(
                        '(', Ws0(),
                        Expressions(), drop(), //push(new ArgumentsNode(pop())),
                        Ws0(),
                        ZeroOrMore(
                                ',', Ws0(),
                                Expressions(), drop(), //peek(1).addChild(pop()),
                                Ws0()
                        ),
                        ')'
                ),
                Sequence('(', Ws0(), ')')//, push(null))
        );
    }

    // ********** LESS Entities **********

    // TODO: Check use of !Nd

    /**
     * Any whitespace delimited token (??)
     */
    Rule Entity() {
        return Sequence(
                /*FirstOf(URL(), AlphaFilter(), Function(), Accessor(), Keyword(), Variable(), Literal(), Font()),*/
                FirstOf(Keyword(), Literal(), URL(), AlphaFilter(), Function(), /*Accessor(), */Variable(), Font()),
                push(new SimpleNode(match()))
        );
    }

    /**
     * 'url(' (String / [-_%$/.&=:;#+?Alphanumeric]+) ')'
     * TODO: Function? Unescape?
     */
    Rule URL() {
        return Sequence(
                "url(",
                FirstOf(
                        String(),
                        OneOrMore(FirstOf(AnyOf("-_%$/.&=:;#+?"), Alphanumeric()))
                ),
                ')'
        );
    }

    /**
     * 'alpha(opacity=' Digit1 ' )
     */
    Rule AlphaFilter() {
        return Sequence("alpha(opacity=", Digit1(), ')');
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
     * Tokens that don't need to evaluated
     */
    Rule Literal() {
        return FirstOf(Color(), MultiDimension(), Dimension(), String());
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
     * Ex: 'hello' / "hello"
     */
    Rule String() {
        return FirstOf(
                Sequence('\'', ZeroOrMore(TestNot('\''), ANY), '\''),
                Sequence('"', ZeroOrMore(TestNot('"'), ANY), '"')
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
    @MemoMismatches
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
        return FirstOf(
                Sequence('#', RGB()),
                Sequence(FirstOf("hsl", "rgb"), Optional('a'), Arguments())
        );
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

    // Avoid storing whitespace / spacing in the AST whenever possible

    Rule Ws0() {
        return ZeroOrMore(Whitespace());
    }

    Rule Ws1() {
        return OneOrMore(Whitespace());
    }

    Rule Whitespace() {
        return AnyOf(" \n");
    }

    Rule Sp0() {
        return ZeroOrMore(Spacing());
    }

    Rule Sp1() {
        return OneOrMore(Spacing());
    }

    Rule Spacing() {
        return FirstOf(Whitespace(), Comment());
    }

    Rule Nd() {
        return Sequence(TestNot(Delimiter()), ANY);
    }

    Rule Delimiter() {
        return AnyOf(" ;,!})\n");
    }

    Rule NameStart() {
        return FirstOf('-', Alpha());
    }

    Rule NameCharacter() {
        return FirstOf(AnyOf("-_"), Alphanumeric());
    }

    // ********** Debugging **********

    boolean debug(Context context) {
        return true;
    }
}
