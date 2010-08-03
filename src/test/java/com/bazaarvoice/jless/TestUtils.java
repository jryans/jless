package com.bazaarvoice.jless;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
        _sLogStream.flush();
    }

    public static void closeLog() {
        _sLogStream.close();
        _sLogFile.deleteOnExit();
    }
}
