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

import difflib.DiffUtils;
import difflib.Patch;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Test
public class TranslatedDiffTest extends ProcessingTest {

    @Override
    protected void runTestFor(String fileName) {
        PROCESSOR.setTranslationEnabled(true);
        LessProcessor.Result result = runProcessor(assembleInput(fileName));
        saveOutput(fileName, result.toString());
        diffOutput(fileName, result.toString());
    }

    @Override
    protected void runTestFor(String parentFileName, String fileName) {
        PROCESSOR.setTranslationEnabled(true);
        LessProcessor.Result result = runProcessor(runProcessor(assembleInput(parentFileName)), assembleInput(fileName));
        saveOutput(fileName, result.toString());
        diffOutput(fileName, result.toString());
    }

    private void diffOutput(String fileName, String output) {
        InputStream referenceStream = getClass().getResourceAsStream("/expected/" + fileName + ".css");
        List<String> referenceLines = null;

        try {
            //noinspection unchecked
            referenceLines = IOUtils.readLines(referenceStream, "UTF-8");
        } catch (IOException e) {
            TestUtils.getLog().println("Unable to read " + fileName + ".css");
            e.printStackTrace();
        }

        List<String> outputLines = Arrays.asList(StringUtils.splitPreserveAllTokens(output, '\n'));

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
