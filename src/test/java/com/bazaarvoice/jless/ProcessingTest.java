package com.bazaarvoice.jless;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@Test
public class ProcessingTest {

    private boolean _translationEnabled;

    protected boolean isTranslationEnabled() {
        return _translationEnabled;
    }

    protected void setTranslationEnabled(boolean translationEnabled) {
        _translationEnabled = translationEnabled;
    }

    protected void runTestFor(String... fileNames) {
        setTranslationEnabled(false);
        List<InputStream> inputs = assembleInputs(fileNames);
        saveOutput(fileNames[fileNames.length - 1], runProcessor(inputs)); // Parsing errors will cause an exception to be thrown
    }

    protected List<InputStream> assembleInputs(String[] fileNames) {
        List<InputStream> inputs = new ArrayList<InputStream>();
        for (String fileName : fileNames) {
            InputStream stream = getClass().getResourceAsStream("/less/" + fileName + ".less");
            Assert.assertNotNull(stream, "Unable to read " + fileName + ".less");
            inputs.add(stream);
        }
        return inputs;
    }

    protected String runProcessor(List<InputStream> inputs) {
        try {
            return new LessProcessor(_translationEnabled).process(inputs).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String saveOutput(String outputFileName, String output) {
        try {
            File generatedDir = new File("test-output/generated/" + getGeneratedDirName());
            if (!generatedDir.exists()) {
                generatedDir.mkdirs();
            }

            PrintStream generatedOutput = new PrintStream(new FileOutputStream(
                    new File(generatedDir, outputFileName + ".out" + getGeneratedFileExtension())));
            generatedOutput.println(output);
            generatedOutput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return output;
    }

    protected String getGeneratedDirName() {
        return "parsed";
    }

    protected String getGeneratedFileExtension() {
        return ".less";
    }

    public void testBazaarvoice() {
        runTestFor("theme", "bazaarvoice");
    }

    public void testBazaarvoiceDisplayShared() {
        runTestFor("theme", "bazaarvoiceDisplayShared");
    }

    public void testBazaarvoiceQA() {
        runTestFor("theme", "bazaarvoiceQA");
    }

    public void testBazaarvoiceSubmitShared() {
        runTestFor("theme", "bazaarvoiceSubmitShared");
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

    public void testIE() {
        runTestFor("ie");
    }

    public void testNested() {
        runTestFor("nested");
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
    