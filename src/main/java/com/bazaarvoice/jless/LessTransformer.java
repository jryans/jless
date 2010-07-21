package com.bazaarvoice.jless;

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

        ParsingResult<?> result = RecoveringParseRunner.run(parser.Document(), ".BVDI_Test { padding: 3px; }");

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
