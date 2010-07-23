package com.bazaarvoice.jless;

import com.bazaarvoice.jless.parser.Parser;
import org.parboiled.Parboiled;
import org.parboiled.RecoveringParseRunner;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.parboiled.trees.GraphNode;
import org.parboiled.trees.GraphUtils;

public class LessTransformer {

    private Parser _parser;

    public LessTransformer() {
        _parser = Parboiled.createParser(Parser.class);
    }

    public Parser getParser() {
        return _parser;
    }

    public static void main(String[] args) {
        LessTransformer transformer = new LessTransformer();

        Parser parser = transformer.getParser();

//        ParsingResult<?> result = RecoveringParseRunner.run(parser.Primary(), ".BVDI_Test { padding: 3px; }");

        ParsingResult<?> result = RecoveringParseRunner.run(parser.Primary(),
                "@x: blue;\n" +
                "@z: transparent;\n" +
                "@mix: none;\n" +
                "\n" +
                ".mixin {\n" +
                "  @mix: #989;\n" +
                "}\n" +
                "\n" +
                ".tiny-scope {\n" +
                "  color: @mix; // #989\n" +
                "  .mixin;\n" +
                "}\n" +
                "\n" +
                ".scope1 {\n" +
                "  @y: orange;\n" +
                "  @z: black;\n" +
                "  color: @x; // blue\n" +
                "  border-color: @z; // black\n" +
                "  .hidden {\n" +
                "    @x: #131313;\n" +
                "  }\n" +
                "  .scope2 {\n" +
                "    @y: red;\n" +
                "    color: @x; // blue\n" +
                "    .scope3 {\n" +
                "      @local: white;\n" +
                "      color: @y; // red\n" +
                "      border-color: @z; // black\n" +
                "      background-color: @local; // white\n" +
                "    }\n" +
                "  }\n" +
                "}");

        if (result.hasErrors()) {
            System.out.println("Parse Errors:\n" + ErrorUtils.printParseErrors(result));
        }

        Object value = result.parseTreeRoot.getValue();

        if (value instanceof GraphNode) {
            System.out.println("Abstract Syntax Tree:\n" +
                    GraphUtils.printTree((GraphNode) value, new ToStringFormatter(null)) + '\n');
        } else {
            System.out.println("Parse Tree:\n" + ParseTreeUtils.printNodeTree(result) + '\n');
        }
    }
}
