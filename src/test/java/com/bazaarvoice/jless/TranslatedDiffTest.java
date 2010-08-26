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
    protected void runTestFor(String... fileNames) {
        setTranslationEnabled(true);
        List<InputStream> inputs = assembleInputs(fileNames);
        String output = runProcessor(inputs);
        saveOutput(fileNames[fileNames.length - 1], output);
        diffOutput(fileNames[fileNames.length - 1], output);
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
