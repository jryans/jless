package com.bazaarvoice.jless;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.List;

@Test
public class TimingTest extends ProcessingTest {

    private static final int RUNS_PER_TIMED_SET = 25;
    private static final String[] WARM_UP_FILES = {"bazaarvoiceDisplayShared", "css", "css-3", "strings", "whitespace"};

    private boolean _warm = false;

    @Override
    protected void runTestFor(String... fileNames) {
        if (!_warm) {
            warmUp();
        }

        List<InputStream> inputs = assembleInputs(fileNames);

        setTranslationEnabled(false);
        timeProcessor(fileNames[fileNames.length - 1], inputs);
        setTranslationEnabled(true);
        timeProcessor(fileNames[fileNames.length - 1], inputs);
    }

    /**
     * Running through the code paths a number of times before benchmarking helps warm up
     * the JVM and helps reach steady-state performance.
     */
    private void warmUp() {
        setTranslationEnabled(true);
        List<InputStream> inputs = assembleInputs(WARM_UP_FILES);

        for (int i = 0; i < RUNS_PER_TIMED_SET; i++) {
            runProcessor(inputs);
        }
        _warm = true;
    }

    private void timeProcessor(String fileName, List<InputStream> inputs) {
        float totalTime = 0, minTime = Long.MAX_VALUE, maxTime = 0, avgTime = 0;
        int i;

        TestUtils.getLog().println("Processing times for " + fileName + ", translation " + (isTranslationEnabled() ? "on" : "off"));

        for (i = 0; i < RUNS_PER_TIMED_SET; i++) {
            long startTime = System.nanoTime();
            runProcessor(inputs);
            long _currentTime = System.nanoTime() - startTime;
            _currentTime /= 1000000;
            totalTime += (System.nanoTime() - startTime) / 1000000;
            if (_currentTime < minTime) {
                minTime = _currentTime;
            }
            if (_currentTime > maxTime) {
                maxTime = _currentTime;
            }
        }

        avgTime = totalTime / i;

        TestUtils.getLog().format("Min. Time: %.3f ms%n", minTime);
        TestUtils.getLog().format("Max. Time: %.3f ms%n", maxTime);
        TestUtils.getLog().format("Avg. Time: %.3f ms%n", avgTime);

        Assert.assertTrue(avgTime <= 60, "Average parsing time for " + fileName + " is larger than 60 ms");
    }
}
