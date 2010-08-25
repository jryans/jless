package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.visitor.Printer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public final class TestUtils {

    private static File _sLogFile;
    private static PrintStream _sLogStream;

    public static PrintStream getLog() {
        if (_sLogStream == null) {
            try {
                _sLogFile = new File("test.log");
                _sLogStream = new PrintStream(new FileOutputStream(_sLogFile));
            } catch (IOException e) {
                System.err.println(e);
                e.printStackTrace();
            }            
        }
        return _sLogStream;
    }

    public static void flushLog() {
        if (_sLogStream == null) {
            return;
        }
        _sLogStream.flush();
    }

    /**
     * Utility method that can be run from within the debugger to look at the parsed output file.
     */
    public static String printParsedResult(Node root) {
        Printer p = new Printer();
        root.traverse(p);
        return p.toString();
    }
}
