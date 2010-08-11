package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.Node;
import org.parboiled.support.ParsingResult;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class ParsingTimeTest extends ParsingTest {

    private static final int RUNS_PER_TIMED_SET = 50;
    private static final String[] CACHE_FILES = {"css", "css-3", "strings", "whitespace"};

    private float _parseTime;
    private boolean _cached = false;

    @Override
    protected void runTestFor(String fileName) {
        if (!_cached) {
            cacheRules();
        }
        timeLessParsing(fileName);
    }

    /**
     * Parboiled caches parsing rules when they are first encountered, so parsing some files first
     * will make our timing measurements more closely reflect continuous usage.
     */
    private void cacheRules() {
        for (int i = 0; i < RUNS_PER_TIMED_SET; i++) {
            for (String fileName : CACHE_FILES) {
                parseLess(fileName, false);
            }
        }
        _cached = true;
    }

    private void timeLessParsing(String fileName) {
        float totalTime = 0, minTime = Long.MAX_VALUE, maxTime = 0, avgTime = 0;
        int i;

        TestUtils.getLog().println("Parse times for " + fileName);

        for (i = 0; i < RUNS_PER_TIMED_SET; i++) {
            parseLess(fileName, false);
            totalTime += _parseTime;
            if (_parseTime < minTime) {
                minTime = _parseTime;
            }
            if (_parseTime > maxTime) {
                maxTime = _parseTime;
            }
        }

        avgTime = totalTime / i;

        TestUtils.getLog().format("Min. Time: %.3f ms%n", minTime);
        TestUtils.getLog().format("Max. Time: %.3f ms%n", maxTime);
        TestUtils.getLog().format("Avg. Time: %.3f ms%n", avgTime);

        Assert.assertTrue(avgTime <= 5, "Average parsing time for " + fileName + " is larger than 5 ms");
    }
    
    @Override
    protected ParsingResult<Node> runParser(String lessInput) {
        long startTime = System.nanoTime();
        ParsingResult<Node> result = super.runParser(lessInput);
        _parseTime = System.nanoTime() - startTime;
         _parseTime /= 1000000;
        return result;
    }
}
