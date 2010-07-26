package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.Node;
import org.apache.commons.io.IOUtils;
import org.parboiled.RecoveringParseRunner;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.parboiled.trees.GraphUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

@Test
public class LessTranslatorTest {

    private static final int RUNS_PER_TIMED_SET = 10;

    private LessTranslator _transformer = new LessTranslator();
    private long _parseTime;

    private ParsingResult<Node> parseLess(String fileName) {
        return parseLess(fileName, true);
    }

    private ParsingResult<Node> parseLess(String fileName, boolean alwaysPrintStatus) {
        InputStream lessStream = getClass().getResourceAsStream("/less/" + fileName + ".less");
        String lessInput = "";
        
        try {
            lessInput = IOUtils.toString(lessStream, "UTF-8");
        } catch (IOException e) {
            System.out.println("Oh no!");
            e.printStackTrace();
        }

        long startTime = System.nanoTime();
        ParsingResult<Node> result = runParser(lessInput);
        _parseTime = System.nanoTime() - startTime;
        _parseTime /= 1000000;

        Assert.assertFalse(result.hasErrors(), getResultStatus(result));

        if (alwaysPrintStatus) {
            System.out.print(getResultStatus(result));
            System.out.flush();
        }

        return result;
    }

    private ParsingResult<Node> runParser(String lessInput) {
        return RecoveringParseRunner.run(_transformer.getParser().Primary(), lessInput);
    }

    private String getResultStatus(ParsingResult<Node> result) {
        StringBuilder sb = new StringBuilder();

        if (result.hasErrors()) {
            sb.append("\nParse Errors:\n").append(ErrorUtils.printParseErrors(result));
        }

        if (result.parseTreeRoot != null) {
            sb.append("Parse Tree:\n").append(ParseTreeUtils.printNodeTree(result)).append('\n');
        }

        if (result.resultValue != null) {
            sb.append("Abstract Syntax Tree:\n").append(GraphUtils.printTree(result.resultValue, new ToStringFormatter<Node>(null))).append('\n');
        }

        return sb.toString();
    }

    private void timeParseLess(String fileName) {
        long totalTime = 0;
        int i;
        System.out.println("Parse times for " + fileName);
        for (i = 0; i < RUNS_PER_TIMED_SET; i++) {
            parseLess(fileName, false);
            System.out.println("Time: " + _parseTime + " ms");
            System.out.flush();
            totalTime += _parseTime;
        }
        System.out.println("Avg. Time: " + (totalTime / i) + " ms");
        System.out.flush();
    }

    public void testParseCss() {
        parseLess("css");
    }

    public void testParseCss3() {
        parseLess("css-3");
    }

    public void testParseBazaarvoiceDisplayShared() {
        parseLess("bazaarvoiceDisplayShared");
    }

    public void testTimeParseBazaarvoiceDisplayShared() {
        timeParseLess("bazaarvoiceDisplayShared");
    }
    
    @AfterMethod
    public void flushOutput() {
        System.out.flush();
    }
}
    