package com.bazaarvoice.jless;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;

@Test
public class TimingTest extends ProcessingTest {

    private static final int RUNS_PER_TIMED_SET = 25;
    private static final String[] WARM_UP_FILES = {"bazaarvoice", "css", "css-3", "strings", "whitespace"};

    private boolean _warm = false;

    @Override
    protected void runTestFor(String fileName) {
        runTestFor(null, fileName);
    }

    @Override
    protected void runTestFor(String parentFileName, String fileName) {
        if (!_warm) {
            warmUp();
        }

        PROCESSOR.setTranslationEnabled(false);
        timeProcessor(parentFileName, fileName);
        PROCESSOR.setTranslationEnabled(true);
        timeProcessor(parentFileName, fileName);
    }

    /**
     * Running through the code paths a number of times before benchmarking helps warm up
     * the JVM and helps reach steady-state performance.
     */
    private void warmUp() {
        PROCESSOR.setTranslationEnabled(true);
        for (int i = 0; i < RUNS_PER_TIMED_SET; i++) {
            for (String fileName : WARM_UP_FILES) {
                runProcessor(runProcessor(assembleInput("theme")), assembleInput(fileName));
            }
        }
        _warm = true;
    }

    private void timeProcessor(String parentFileName, String fileName) {
        float totalTime = 0, minTime = Float.MAX_VALUE, maxTime = 0, avgTime;
        int i;

        TestUtils.getLog().println("Processing times for " + fileName + ", translation " + (PROCESSOR.isTranslationEnabled() ? "on" : "off"));

        for (i = 0; i < RUNS_PER_TIMED_SET; i++) {
            InputStream parentInput = null;
            if (parentFileName != null) {
                parentInput = assembleInput(parentFileName);
            }
            InputStream input = assembleInput(fileName);
            long startTime = System.nanoTime();
            if (parentInput != null) {
                runProcessor(runProcessor(parentInput), input);
            } else {
                runProcessor(input);
            }
            float runTime = System.nanoTime() - startTime;
            runTime /= 1000000;
            totalTime += runTime;
            if (runTime < minTime) {
                minTime = runTime;
            }
            if (runTime > maxTime) {
                maxTime = runTime;
            }
        }

        avgTime = totalTime / i;

        TestUtils.getLog().format("Min. Time: %.3f ms%n", minTime);
        TestUtils.getLog().format("Max. Time: %.3f ms%n", maxTime);
        TestUtils.getLog().format("Avg. Time: %.3f ms%n", avgTime);

        Assert.assertTrue(avgTime <= 60, "Average parsing time for " + fileName + " is larger than 60 ms");
    }
}
