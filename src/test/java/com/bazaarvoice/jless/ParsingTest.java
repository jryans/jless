package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.print.Printer;
import org.apache.commons.io.IOUtils;
import org.parboiled.ReportingParseRunner;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.parboiled.trees.GraphUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

@Test
public class ParsingTest {

    private LessTranslator _transformer = new LessTranslator();

    protected ParsingResult<Node> parseLess(String fileName) {
        return parseLess(fileName, true);
    }

    protected ParsingResult<Node> parseLess(String fileName, boolean alwaysPrintStatus) {
        InputStream lessStream = getClass().getResourceAsStream("/less/" + fileName + ".less");
        String lessInput = "";
        
        try {
            lessInput = IOUtils.toString(lessStream, "UTF-8");
        } catch (IOException e) {
            TestUtils.getLog().println("Unable to read " + fileName + ".less");
            e.printStackTrace();
        }

        ParsingResult<Node> result = runParser(lessInput);

        if (alwaysPrintStatus) {
            TestUtils.getLog().print(getResultStatus(result));
        }

        Assert.assertFalse(result.hasErrors(), getResultStatus(result));

        return result;
    }

    protected ParsingResult<Node> runParser(String lessInput) {
        return ReportingParseRunner.run(_transformer.getParser().Document(), lessInput);
    }

    private String getResultStatus(ParsingResult<Node> result) {
        StringBuilder sb = new StringBuilder();

        if (result.hasErrors()) {
            sb.append("\nParse Errors:\n").append(ErrorUtils.printParseErrors(result));
        }

        /*if (result.parseTreeRoot != null) {
            sb.append("Parse Tree:\n").append(ParseTreeUtils.printNodeTree(result)).append('\n');
        }*/

        if (result.resultValue != null) {
            sb.append("Input AST:\n").append(GraphUtils.printTree(result.resultValue, new ToStringFormatter<Node>(null))).append('\n');

//            result.resultValue.accept(new FlattenNestedRuleSets());

//            sb.append("Output AST:\n").append(GraphUtils.printTree(result.resultValue, new ToStringFormatter<Node>(null))).append('\n');
            
            sb.append(printResult(result));
        }

        return sb.toString();
    }

    protected String printResult(ParsingResult<Node> result) {
        return printResult(result.resultValue);
    }

    public static String printResult(Node root) {
        Printer p = new Printer();
        root.accept(p);
        return p.toString();
    }
    
    protected void runTestFor(String fileName) {
        parseLess(fileName);
    }

    public void testCss() {
        runTestFor("css");
    }

    public void testCss3() {
        runTestFor("css-3");
    }

    public void testSelectors() {
        runTestFor("selectors");
    }

    public void testStrings() {
        runTestFor("strings");
    }

    public void testWhitespace() {
        runTestFor("whitespace");
    }

    /*public void testBazaarvoiceDisplayShared() {
        runTestFor("bazaarvoiceDisplayShared");
    }*/

    @AfterMethod
    public void flushOutput() {
        TestUtils.flushLog();
    }
}
    