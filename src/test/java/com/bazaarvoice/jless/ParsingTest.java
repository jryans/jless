package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.ast.visitor.Printer;
import org.apache.commons.io.IOUtils;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.support.ParsingResult;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

@Test
public class ParsingTest {

    protected ParsingResult<Node> parseLess(String fileName) {
        return parse(fileName, true);
    }

    protected ParsingResult<Node> parse(String fileName, boolean alwaysPrintStatus) {
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
        return LessTranslator.parse(lessInput);
    }
    
    protected void runTranslator(ParsingResult<Node> parseResult) {
        LessTranslator.translate(parseResult.resultValue);
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
//            sb.append("Input AST:\n").append(GraphUtils.printTree(result.resultValue, new ToStringFormatter<Node>(null))).append('\n');

//            sb.append(printResult(result));
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

    public void testBazaarvoice() {
        runTestFor("bazaarvoice");
    }

    public void testBazaarvoiceDisplayShared() {
        runTestFor("bazaarvoiceDisplayShared");
    }

    public void testComments() {
        runTestFor("comments");
    }

    public void testCss() {
        runTestFor("css");
    }

    public void testCss3() {
        runTestFor("css-3");
    }

    public void testDashPrefix() {
        runTestFor("dash-prefix");
    }

    public void testLazyEval() {
        runTestFor("lazy-eval");
    }

    public void testLessBright() {
        runTestFor("less-bright");
    }

    public void testMixins() {
        runTestFor("mixins");
    }

    public void testRuleSets() {
        runTestFor("rulesets");
    }

    public void testScope() {
        runTestFor("scope");
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

    @AfterMethod
    public void flushOutput() {
        TestUtils.flushLog();
    }
}
    