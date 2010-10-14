package com.bazaarvoice.jless.parser;

import com.bazaarvoice.jless.ast.node.ArgumentsNode;
import com.bazaarvoice.jless.ast.node.ExpressionGroupNode;
import com.bazaarvoice.jless.ast.node.ExpressionNode;
import com.bazaarvoice.jless.ast.node.ExpressionPhraseNode;
import com.bazaarvoice.jless.ast.node.FilterArgumentNode;
import com.bazaarvoice.jless.ast.node.FunctionNode;
import com.bazaarvoice.jless.ast.node.LineBreakNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ParametersNode;
import com.bazaarvoice.jless.ast.node.PlaceholderNode;
import com.bazaarvoice.jless.ast.node.PropertyNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorGroupNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.node.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.node.SimpleNode;
import com.bazaarvoice.jless.ast.node.SpacingNode;
import com.bazaarvoice.jless.ast.node.VariableDefinitionNode;
import com.bazaarvoice.jless.ast.node.VariableReferenceNode;
import com.bazaarvoice.jless.ast.util.NodeTreeUtils;
import com.bazaarvoice.jless.exception.UndefinedMixinException;
import com.bazaarvoice.jless.exception.UndefinedVariableException;
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
 * Parsing differences from LESS Ruby include:
 * <ul>
 *   <li>Selector parsing was rewritten using the <a href="http://www.w3.org/TR/css3-selectors/#grammar">CSS 3 selector grammar</a></li> 
 *   <li>Numbers in attribute selectors must be quoted (as per the CSS spec)</li>
 *   <li>Case-sensitivity was removed</li>
 *   <li>Line breaks can be used in all places where spaces are accepted</li>
 * </ul>
 *
 * This parser attempts to track line breaks so that the input and output files can have the same number of lines
 * (which is helpful when referencing styles via browser tools like Firebug).
 *
 * This list only notes changes in the <em>parsing</em> stage. See {@link com.bazaarvoice.jless.LessProcessor} for details on any changes
 * to the <em>translation</em> stage.
 *
 * @see com.bazaarvoice.jless.LessProcessor
 */
public class Parser extends BaseParser<Node> {

    private boolean _parserTranslationEnabled;

    public Parser() {
        this(true);
    }

    public Parser(Boolean parserTranslationEnabled) {
        _parserTranslationEnabled = parserTranslationEnabled;
    }

    protected boolean isParserTranslationEnabled() {
        return _parserTranslationEnabled;
    }

    protected Node makeSpacingNode() {
        return new LineBreakNode(match());
    }

    // ********** Document **********

    public Rule Document() {
        return Sequence(Scope(), EOI);
    }

    /**
     * This is the high-level rule at some scope (either the root document or within a rule set / mixin).
     * Future: Imports
     */
    Rule Scope() {
        return Sequence(
                push(new ScopeNode()),
                ZeroOrMore(
                        FirstOf(
                                Declaration(),
                                RuleSet(),
                                MixinReference(),
                                Sequence(Sp1(), push(new SpacingNode(match())))
                        ),
                        peek(1).addChild(pop())
                )
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
                                '{',
                                Scope(), peek(1).addChild(pop()), Ws0()
                        ),
                        // Mixin rule set, with possible arguments
                        Sequence(
                                ClassSelectorGroup(), push(new RuleSetNode(pop())), Ws0(),
                                Parameters(), Ws0(),
                                '{',
                                Scope(), peek(1).addChild(pop()), peek(1).addChild(pop()), Ws0()
                        )
                ),
                '}', Ws0(), peek().addChild(makeSpacingNode())
        );
    }

    /**
     * '(' Parameter Ws0 (',' Ws0 Parameter)* ')'
     */
    Rule Parameters() {
        return Sequence(
                '(',
                Parameter(), push(new ParametersNode(pop())), Ws0(),
                ZeroOrMore(
                        ',', Ws0(),
                        Parameter(), peek(1).addChild(pop())
                ),
                ')',
                push(new ScopeNode(pop()))
        );
    }

    /**
     * Variable Ws0 ':' Ws0 ExpressionPhrase
     */
    Rule Parameter() {
        return Sequence(
                Variable(), push(new VariableDefinitionNode(match())), peek().setVisible(!isParserTranslationEnabled()), Ws0(),
                ':', Ws0(),
                ExpressionPhrase(), peek(1).addChild(new ExpressionGroupNode(pop()))
        );
    }

    /**
     * SelectorGroup ';' Ws0
     * / Class Arguments ';' Ws0
     */
    @MemoMismatches
    Rule MixinReference() {
        Var<String> name = new Var<String>();
        return FirstOf(
                // No arguments, reference an existing rule set's properties
                Sequence(
                        SelectorGroup(), ';',
                        resolveMixinReference(pop().toString(), null),
                        Ws0(), peek().addChild(makeSpacingNode())
                ),
                // Call a mixin, passing along some arguments
                Sequence(
                        Class(), name.set(match()), Arguments(), ';',
                        resolveMixinReference(name.get(), (ArgumentsNode) pop()),
                        Ws0(), peek().addChild(makeSpacingNode())
                )
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
                        ',', Ws0(), peek().addChild(makeSpacingNode()),
                        Selector(), peek(1).addChild(pop())
                ),
                Ws0(), peek().addChild(makeSpacingNode())
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
                Optional(SymbolCombinator()),
                selectorSegmentNode.set(new SelectorSegmentNode(match())),
                SimpleSelector(selectorSegmentNode),
                selectorSegmentNode.get().setSimpleSelector(match()),
                peek().addChild(selectorSegmentNode.getAndClear()),
                // Additional selector segments must have a combinator
                ZeroOrMore(
                        Combinator(),
                        selectorSegmentNode.set(new SelectorSegmentNode(match())),
                        SimpleSelector(selectorSegmentNode),
                        selectorSegmentNode.get().setSimpleSelector(match()),
                        peek().addChild(selectorSegmentNode.getAndClear())
                )
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
                        FirstOf(
                                ID(), Class(), Attribute(), Negation(), ElementName(),
                                Sequence(
                                        FirstOf(
                                                UniversalHtml(),
                                                Universal()
                                        ),
                                        selectorSegmentNode.get().setUniversal(true)
                                ),
                                Sequence(
                                        Pseudo(),
                                        selectorSegmentNode.get().setSubElementSelector(true)
                                )
                        ),
                        ZeroOrMore(FirstOf(ID(), Class(), Attribute(), Negation(), Pseudo()))
                ),
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

    Rule PseudoExpression() {
        return OneOrMore(FirstOf(AnyOf("+-"), Dimension(), Digit(), String(), Ident()), Ws0());
    }

    Rule Negation() {
        return Sequence(":not(", Ws0(), NegationArgument(), Ws0(), ')');
    }

    Rule NegationArgument() {
        return FirstOf(ElementName(), Universal(), ID(), Class(), Attribute(), Pseudo());
    }

    // ********** Variables & Expressions **********

    /**
     * (Ident / Variable) Ws0 ':' Ws0 ExpressionPhrase (Ws0 ',' Ws0 ExpressionPhrase)* Sp0 (';' / Ws0 &'}')
     * / Ident Ws0 ':' Ws0 ';'
     *
     * Ex: @my-var: 12px; height: 100%;
     */
    Rule Declaration() {
        return FirstOf(
                Sequence(
                        FirstOf(
                                Sequence(PropertyName(), push(new PropertyNode(match()))),
                                Sequence(Variable(), push(new VariableDefinitionNode(match())), peek().setVisible(!isParserTranslationEnabled()))
                        ), Ws0(),
                        ':', Ws0(),
                        push(new ExpressionGroupNode()),
                        ExpressionPhrase(), peek(1).addChild(pop()),
                        ZeroOrMore(
                                Ws0(), ',', Ws0(), peek().addChild(makeSpacingNode()),
                                ExpressionPhrase(), peek(1).addChild(pop())
                        ),
                        Sp0(), FirstOf(';', Sequence(Ws0(), Test('}'))),
                        peek(1).addChild(pop())
                ),
                // Empty rules are ignored
                Sequence(
                        Ident(), push(new PlaceholderNode()), Ws0(),
                        ':', Ws0(),
                        ';'
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
    Rule ExpressionPhrase() {
        // Space-separated expressions
        return Sequence(
                Expression(), push(new ExpressionPhraseNode(pop())),
                ZeroOrMore(Ws1(), Expression(), peek(1).addChild(pop())),
                Optional(Ws0(), Important(), peek(1).addChild(pop()))
        );
    }

    /**
     * '(' Ws0 ExpressionPhrase Ws0 ')' Future: Operations
     * / Entity
     */
    Rule Expression() {
        return Sequence(Value(), push(new ExpressionNode(pop())));
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
        return Sequence(Universal(), Ws0(), "html");
    }

    // ********** CSS Entities **********

    Rule ElementName() {
        return Ident();
    }

    Rule Universal() {
        return Ch('*');
    }

    Rule ID() {
        return Sequence('#', Name());
    }

    @MemoMismatches
    Rule Class() {
        return Sequence('.', Ident());
    }

    // ********** Functions & Arguments **********

    /**
     * Ident Arguments
     */
    Rule Function() {
        return Sequence(
                Ident(), push(new FunctionNode(match())),
                Arguments(), peek(1).addChild(pop())
        );
    }

    /**
     * '(' Ws0 ExpressionPhrase Ws0 (',' Ws0 ExpressionPhrase Ws0)* ')' / '(' Ws0 ')'
     */
    Rule Arguments() {
        return FirstOf(
                Sequence(
                        '(', Ws0(),
                        ExpressionPhrase(), push(new ArgumentsNode(new ExpressionGroupNode(pop()))),
                        Ws0(),
                        ZeroOrMore(
                                ',', Ws0(),
                                ExpressionPhrase(), peek(1).addChild(new ExpressionGroupNode(pop())),
                                Ws0()
                        ),
                        ')'
                ),
                Sequence('(', Ws0(), ')', push(new ArgumentsNode()))
        );
    }

    /**
     * Ex: progid:DXImageTransform.Microsoft.gradient(startColorstr=@lightColor, endColorstr=@darkColor)
     */
    Rule FilterFunction() {
        return Sequence(
                Sequence(
                        "progid:",
                        OneOrMore(FirstOf('.', Ident()))
                ),
                push(new FunctionNode(match())),
                FilterArguments(),
                peek(1).addChild(pop())
        );
    }

    /**
     * '(' Ws0 FilterArgument Ws0 (',' Ws0 FilterArgument Ws0)* ')' / '(' Ws0 ')'
     */
    Rule FilterArguments() {
        return FirstOf(
                Sequence(
                        '(', Ws0(),
                        FilterArgument(), push(new ArgumentsNode(pop())),
                        Ws0(),
                        ZeroOrMore(
                                ',', Ws0(),
                                FilterArgument(), peek(1).addChild(pop()),
                                Ws0()
                        ),
                        ')'
                ),
                Sequence('(', Ws0(), ')', push(new ArgumentsNode()))
        );
    }

    /**
     * Ex: startColorstr=@lightColor
     */
    Rule FilterArgument() {
        return Sequence(
                Ident(), push(new FilterArgumentNode(match())), Ws0(),
                '=', Ws0(),
                Value(), peek(1).addChild(pop())
        );
    }

    // ********** LESS Entities **********

    /**
     * Any token used as a value in an expression
     * Future: Accessors
     */
    Rule Value() {
        return FirstOf(
                Keyword(),
                Literal(),
                Function(),
                VariableReference(),
                URL(),
                Font(),
                AlphaFilter(),
                ExpressionFunction(),
                FilterFunction()
        );
    }

    @MemoMismatches
    Rule VariableReference() {
        return Sequence(
                Variable(),
                pushVariableReference(match())
        );
    }

    /**
     * 'url(' (String / [-_%$/.&=:;#+?Alphanumeric]+) ')'
     */
    Rule URL() {
        return Sequence(
                Sequence(
                        "url(", Ws0(),
                        FirstOf(
                                String(),
                                OneOrMore(FirstOf(AnyOf("-_%$/.&=:;#+?"), Alphanumeric()))
                        ), Ws0(),
                        ')'
                ),
                push(new SimpleNode(match()))
        );
    }

    /**
     * 'alpha(' Ws0 'opacity' Ws0 '=' Ws0 Digit1 Ws0 ')'
     */
    Rule AlphaFilter() {
        return Sequence(
                Sequence(
                        "alpha(", Ws0(),
                        "opacity", Ws0(),
                        '=', Ws0(),
                        Digit1(), Ws0(),
                        ')'
                ),
                push(new SimpleNode(match()))
        );
    }

    /**
     * 'expression(' (!(')' Ws0 [;}]) .)* ');'
     */
    Rule ExpressionFunction() {
        return Sequence(
                Sequence(
                        "expression(",
                        ZeroOrMore(
                                TestNot(
                                        ')',
                                        Ws0(),
                                        AnyOf(";}")
                                ),
                                ANY
                        ),
                        ')'
                ),
                push(new SimpleNode(match()))
        );
    }

    /**
     * Ident &Delimiter
     *
     * Ex: blue, small, normal
     */
    Rule Keyword() {
        return Sequence(
                Sequence(Ident(), Test(Delimiter())),
                push(new SimpleNode(match()))
        );
    }

    /**
     * Tokens that don't need to evaluated
     */
    Rule Literal() {
        return Sequence(
                FirstOf(Color(), MultiDimension(), Dimension(), String()),
                push(new SimpleNode(match()))
        );
    }

    /**
     * Alpha [-Alphanumeric]* &Delimiter / String
     */
    Rule Font() {
        return Sequence(
                FirstOf(
                        Sequence(Alpha(), ZeroOrMore(FirstOf('-', Alphanumeric())), Test(Delimiter())),
                        String()
                ),
                push(new SimpleNode(match()))
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
     * (Dimension / Ident) '/' Dimension
     */
    Rule MultiDimension() {
        return Sequence(
                FirstOf(
                        Dimension(),
                        Ident()
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
     * '#' RGB
     */
    Rule Color() {
        return Sequence('#', RGB());
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

    // ********** Comments **********

    Rule Comment() {
        return FirstOf(MultipleLineComment(), SingleLineComment());
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
        return AnyOf(" \n\t");
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

    Rule Delimiter() {
        return FirstOf(AnyOf(";,!})"), Whitespace());
    }

    Rule NameStart() {
        return FirstOf('_', Alpha());
    }

    Rule NameCharacter() {
        return FirstOf(AnyOf("-_"), Alphanumeric());
    }

    @MemoMismatches
    Rule Ident() {
        return Sequence(Optional('-'), NameStart(), ZeroOrMore(NameCharacter()));
    }

    Rule Name() {
        return OneOrMore(NameCharacter());
    }

    // ********** Translation Actions **********

    /**
     * Locates the referenced mixin in one of the scope nodes on the stack. If found, the mixin's
     * scope is cloned and placed onto the stack in place of the mixin reference. Additionally,
     * any arguments are applied to the mixin's scope.
     */
    boolean resolveMixinReference(String name, ArgumentsNode arguments) {
        if (!isParserTranslationEnabled()) {
            return push(new PlaceholderNode(new SimpleNode(name)));
        }

        // Walk down the stack, looking for a scope node that knows about a given rule set
        for (Node node : getContext().getValueStack()) {
            if (!(node instanceof ScopeNode)) {
                continue;
            }

            ScopeNode scope = (ScopeNode) node;
            RuleSetNode ruleSet = scope.getRuleSet(name);

            if (ruleSet == null) {
                continue;
            }

            // Get the scope of the rule set we located and call it as a mixin
            ScopeNode ruleSetScope = NodeTreeUtils.getFirstChild(ruleSet, ScopeNode.class).callMixin(name, arguments);

            return push(ruleSetScope);
        }

        // Record error location
        throw new UndefinedMixinException(name);
    }

    /**
     * Looks for a variable definition that matches the reference in the scope nodes on the stack.
     * If found, a reference node that can repeat this lookup later is placed on the stack, not the
     * current value itself. This is done because the value may change if the variable reference is
     * inside a mixin.
     */
    boolean pushVariableReference(String name) {
        if (!isParserTranslationEnabled()) {
            return push(new SimpleNode(name));
        }

        // Walk down the stack, looking for a scope node that knows about a given variable
        for (Node node : getContext().getValueStack()) {
            if (!(node instanceof ScopeNode)) {
                continue;
            }

            // Ensure that the variable exists
            ScopeNode scope = (ScopeNode) node;
            if (!scope.isVariableDefined(name)) {
                continue;
            }

            return push(new VariableReferenceNode(name));
        }

        // Record error location
        throw new UndefinedVariableException(name);
    }

    // ********** Debugging **********

    boolean debug(Context context) {
        return true;
    }
}
