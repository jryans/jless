package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.Node;
import org.parboiled.support.ParsingResult;
import org.testng.annotations.Test;

@Test
public class LessTranslatorTimeTest extends LessTranslatorParsingTest {

    private static final int RUNS_PER_TIMED_SET = 50;

    private long _parseTime;

    @Override
    protected void runTestFor(String fileName) {
        timeLessParsing(fileName);
    }

    private void timeLessParsing(String fileName) {
        long totalTime = 0, minTime = Long.MAX_VALUE, maxTime = 0;
        int i;
        System.out.println("Parse times for " + fileName);
        for (i = 0; i < RUNS_PER_TIMED_SET; i++) {
            parseLess(fileName, false);
//            System.out.println("Time: " + _parseTime + " ms");
//            System.out.flush();
            totalTime += _parseTime;
            if (_parseTime < minTime) {
                minTime = _parseTime;
            }
            if (_parseTime > maxTime) {
                maxTime = _parseTime;
            }
        }
        System.out.println("Min. Time: " + minTime + " ms");
        System.out.println("Max. Time: " + maxTime + " ms");
        System.out.println("Avg. Time: " + (totalTime / i) + " ms");
        System.out.flush();
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
