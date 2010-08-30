package com.bazaarvoice.jless;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.List;

@Test
public class TimingTest extends ProcessingTest {

    private static final int RUNS_PER_TIMED_SET = 25;
    private static final String[] WARM_UP_FILES = {"theme", "bazaarvoice", "css", "css-3", "strings", "whitespace"};

    private boolean _warm = false;

    @Override
    protected void runTestFor(String... fileNames) {
        if (!_warm) {
            warmUp();
        }

        setTranslationEnabled(false);
        timeProcessor(fileNames);
        setTranslationEnabled(true);
        timeProcessor(fileNames);
    }

    /**
     * Running through the code paths a number of times before benchmarking helps warm up
     * the JVM and helps reach steady-state performance.
     */
    private void warmUp() {
        setTranslationEnabled(true);
        for (int i = 0; i < RUNS_PER_TIMED_SET; i++) {
            runProcessor(assembleInputs(WARM_UP_FILES));
        }
        _warm = true;
    }

    private void timeProcessor(String... fileNames) {
        String outputFileName = fileNames[fileNames.length - 1];
        float totalTime = 0, minTime = Float.MAX_VALUE, maxTime = 0, avgTime;
        int i;

        TestUtils.getLog().println("Processing times for " + outputFileName + ", translation " + (isTranslationEnabled() ? "on" : "off"));

        for (i = 0; i < RUNS_PER_TIMED_SET; i++) {
            List<InputStream> inputs = assembleInputs(fileNames);
            long startTime = System.nanoTime();
            runProcessor(inputs);
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

        Assert.assertTrue(avgTime <= 60, "Average parsing time for " + outputFileName + " is larger than 60 ms");
    }
}
