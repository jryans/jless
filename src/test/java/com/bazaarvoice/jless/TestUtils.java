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

import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.visitor.Printer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public final class TestUtils {

    private static PrintStream _sLogStream;

    public static PrintStream getLog() {
        if (_sLogStream == null) {
            try {
                File log = new File("test.log");
                _sLogStream = new PrintStream(new FileOutputStream(log));
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
