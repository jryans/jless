/**
 * Copyright 2010 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author J. Ryan Stinnett (ryan.stinnett@bazaarvoice.com)
 */

package com.bazaarvoice.jless;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

@Test
public class ProcessingTest {

    protected static final LessProcessor PROCESSOR = new LessProcessor();

    protected void runTestFor(String fileName) {
        PROCESSOR.setTranslationEnabled(false);
        saveOutput(fileName, runProcessor(assembleInput(fileName)).toString());
    }

    protected void runTestFor(String parentFileName, String fileName) {
        PROCESSOR.setTranslationEnabled(false);
        saveOutput(fileName, runProcessor(runProcessor(assembleInput(parentFileName)), assembleInput(fileName)).toString());
    }

    protected InputStream assembleInput(String fileName) {
        InputStream stream = getClass().getResourceAsStream("/less/" + fileName + ".less");
        Assert.assertNotNull(stream, "Unable to read " + fileName + ".less");
        return stream;
    }

    protected LessProcessor.Result runProcessor(InputStream input) {
        return runProcessor(null, input);
    }

    protected LessProcessor.Result runProcessor(LessProcessor.Result parent, InputStream input) {
        try {
            return PROCESSOR.process(parent, input);
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
                    new File(generatedDir, outputFileName + getGeneratedFileExtension())));
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

    public void testCommentsMac() {
        runTestFor("comments-mac");
    }

    public void testCommentsWindows() {
        runTestFor("comments-windows");
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

    public void testMixinsArgsMinimal() {
        runTestFor("mixins-args-minimal");
    }

    public void testNested() {
        runTestFor("nested");
    }

    public void testNestedHybrid() {
        runTestFor("nested-hybrid");
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
    