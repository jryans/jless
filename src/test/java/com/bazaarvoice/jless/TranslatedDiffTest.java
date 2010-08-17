package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.parser.Parser;
import difflib.DiffUtils;
import difflib.Patch;
import org.apache.commons.io.IOUtils;
import org.parboiled.Parboiled;
import org.parboiled.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Test
public class TranslatedDiffTest extends ParsingTest {

    @Override
    protected void runTestFor(String fileName) {
        ParsingResult<Node> result = parseLess(fileName);

        LessTranslator.translate(result.resultValue);

//        TestUtils.getLog().println("Output AST:");
//        TestUtils.getLog().println(GraphUtils.printTree(result.resultValue, new ToStringFormatter<Node>(null)));

        diffOutput(fileName, result);
    }

    protected ParsingResult<Node> runParser(String lessInput) {
        return ReportingParseRunner.run(Parboiled.createParser(Parser.class, true).Document(), lessInput);
    }

    private void diffOutput(String fileName, ParsingResult<Node> parsingResult) {
        InputStream referenceStream = getClass().getResourceAsStream("/expected/" + fileName + ".css");
        List<String> referenceLines = null;

        try {
            //noinspection unchecked
            referenceLines = IOUtils.readLines(referenceStream, "UTF-8");
        } catch (IOException e) {
            TestUtils.getLog().println("Unable to read " + fileName + ".css");
            e.printStackTrace();
        }

        List<String> outputLines = Arrays.asList(printResult(fileName, parsingResult).split("\n"));

        Patch diff = DiffUtils.diff(referenceLines, outputLines);

        if (!diff.getDeltas().isEmpty()) {
            TestUtils.getLog().println("Reference output diff:");
            List<String> diffOutput = DiffUtils.generateUnifiedDiff(fileName + ".css", fileName + ".css", referenceLines, diff, 3);
            for (String diffOutputLine : diffOutput) {
                TestUtils.getLog().println(diffOutputLine);
            }
        }

        Assert.assertEquals(diff.getDeltas().size(), 0);
    }

    @Override
    protected String getGeneratedDirName() {
        return "translated";
    }

    @Override
    protected String getGeneratedFileExtension() {
        return ".css";
    }
}
