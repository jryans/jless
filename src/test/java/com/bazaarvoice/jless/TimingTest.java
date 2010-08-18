package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.Node;
import org.parboiled.support.ParsingResult;
import org.testng.annotations.Test;

@Test
public class TimingTest extends ParsingTest {

    private static final int RUNS_PER_TIMED_SET = 20;
    private static final String[] CACHE_FILES = {"bazaarvoice", "bazaarvoiceDisplayShared", "css", "css-3", "strings", "whitespace"};
//    private static final ProfilingParseRunner<BaseTreeNode> _sParseRunner = new ProfilingParseRunner<BaseTreeNode>(Parboiled.createParser(Parser.class).Document());

    private float _currentTime;
    private boolean _cached = false;

    @Override
    protected void runTestFor(String fileName) {
        if (!_cached) {
            cacheRules();
        }
        ParsingResult<Node> result = timeParsing(fileName);
        timeTranslation(fileName, result);
    }

    /**
     * Parboiled caches parsing rules when they are first encountered, so parsing some files first
     * will make our timing measurements more closely reflect continuous usage.
     */
    private void cacheRules() {
        for (int i = 0; i < RUNS_PER_TIMED_SET; i++) {
            for (String fileName : CACHE_FILES) {
                parse(fileName, false);
            }
        }
//        TestUtils.getLog().println(_sParseRunner.getReport().print());
//        TestUtils.flushLog();
        _cached = true;
    }

    private ParsingResult<Node> timeParsing(String fileName) {
        float totalTime = 0, minTime = Long.MAX_VALUE, maxTime = 0, avgTime = 0;
        int i;

        TestUtils.getLog().println("Parse times for " + fileName);

        ParsingResult<Node> result = null;

        for (i = 0; i < RUNS_PER_TIMED_SET; i++) {
            result = parse(fileName, false);
            totalTime += _currentTime;
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

//        Assert.assertTrue(avgTime <= 5, "Average parsing time for " + fileName + " is larger than 5 ms");

        return result;
    }
    
    private void timeTranslation(String fileName, ParsingResult<Node> parseResult) {
        float totalTime = 0, minTime = Long.MAX_VALUE, maxTime = 0, avgTime = 0;
        int i;

        TestUtils.getLog().println("Translation times for " + fileName);

        for (i = 0; i < RUNS_PER_TIMED_SET; i++) {
            runTranslator(parseResult);
            totalTime += _currentTime;
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

//        Assert.assertTrue(avgTime <= 5, "Average translation time for " + fileName + " is larger than 5 ms");
    }

    @Override
    protected ParsingResult<Node> runParser(String lessInput) {
        long startTime = System.nanoTime();
//        ParsingResult<BaseTreeNode> result = _sParseRunner.run(lessInput);
        ParsingResult<Node> result = super.runParser(lessInput);
        _currentTime = System.nanoTime() - startTime;
         _currentTime /= 1000000;
        return result;
    }

    @Override
    protected void runTranslator(ParsingResult<Node> parseResult) {
        long startTime = System.nanoTime();
        super.runTranslator(parseResult);
        _currentTime = System.nanoTime() - startTime;
        _currentTime /= 1000000;
    }
}
