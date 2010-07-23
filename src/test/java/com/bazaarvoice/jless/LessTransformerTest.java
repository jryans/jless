package com.bazaarvoice.jless;

import org.apache.commons.io.IOUtils;
import org.parboiled.BasicParseRunner;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.parboiled.trees.GraphNode;
import org.parboiled.trees.GraphUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

@Test
public class LessTransformerTest {

    private LessTransformer transformer = new LessTransformer();

    private String parseLess(String fileName) {
        InputStream lessStream = getClass().getResourceAsStream("/less/" + fileName + ".less");
        String lessOutput = "";
        try {
            String lessInput = IOUtils.toString(lessStream, "UTF-8");

            long time = System.currentTimeMillis();
//            ParsingResult<?> result = RecoveringParseRunner.run(transformer.getParser().Primary(), lessInput);
            ParsingResult<?> result = BasicParseRunner.run(transformer.getParser().Primary(), lessInput);
            System.out.println("Time: " + (System.currentTimeMillis() - time) + " ms");

            Assert.assertFalse(result.hasErrors(), getResultStatus(result));

            System.out.println(getResultStatus(result));
        } catch (IOException e) {
            System.out.println("Oh no!");
            e.printStackTrace();
        }
        return lessOutput;
    }

    private String getResultStatus(ParsingResult<?> result) {
        StringBuilder sb = new StringBuilder().append('\n');

        if (result.hasErrors()) {
            sb.append("Parse Errors:\n").append(ErrorUtils.printParseErrors(result));
        }

        Object value = result.parseTreeRoot.getValue();

        if (value instanceof GraphNode) {
            sb.append("Abstract Syntax Tree:\n").append(GraphUtils.printTree((GraphNode) value, new ToStringFormatter(null))).append('\n');
        } else {
            sb.append("Parse Tree:\n").append(ParseTreeUtils.printNodeTree(result)).append('\n');
        }

        return sb.toString();
    }

    public void testCss() {
        parseLess("css");
    }

    public void testCss3() {
        parseLess("css-3");
    }

    public void testBazaarvoiceDisplayShared() {
        parseLess("bazaarvoiceDisplayShared");
    }
}
    