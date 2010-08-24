package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.visitor.TranslatedPrinter;
import com.bazaarvoice.jless.exception.LessTranslationException;
import com.bazaarvoice.jless.parser.Parser;
import org.apache.commons.io.IOUtils;
import org.parboiled.Parboiled;
import org.parboiled.ParseRunner;
import org.parboiled.ReportingParseRunner;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.support.DefaultValueStack;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ValueStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The translator is not thread-safe...
 *
 * TODO: Doc
 */
public class LessTranslator {

    private Parser _parser;

    public LessTranslator() {
        _parser = Parboiled.createParser(Parser.class);
    }

    public String translate(File file) throws IOException {
        return translateFiles(Collections.singletonList(file));
    }

    public String translate(InputStream stream) throws IOException {
        return translateStreams(Collections.singletonList(stream));
    }

    public String translate(String input) {
        return translateStrings(Collections.singletonList(input));
    }

    public String translateFiles(List<File> files) throws IOException {
        List<InputStream> streams = new ArrayList<InputStream>();
        for (File file : files) {
            streams.add(new FileInputStream(file));
        }
        return translateStreams(streams);
    }

    public String translateStreams(List<InputStream> streams) throws IOException {
        return translateStreams(streams, Collections.<String>emptyList());
    }

    public String translateStreams(List<InputStream> streams, List<String> paths) throws IOException {
        List<String> strings = new ArrayList<String>();
        for (InputStream stream : streams) {
            strings.add(IOUtils.toString(stream, "UTF-8"));
        }
        return translateStrings(strings);
    }

    public String translateStrings(List<String> inputs) {
        ValueStack<Node> stack = new DefaultValueStack<Node>();

        for (String input : inputs) {
            ParseRunner<Node> parseRunner = new ReportingParseRunner<Node>(_parser.Document(), stack);
            ParsingResult<Node> result = parseRunner.run(input);

            if (result.hasErrors()) {
                throw new LessTranslationException("An error occurred while translating a LESS input file:\n" + 
                        ErrorUtils.printParseErrors(result));
            }

            // If there are multiple scope nodes on the stack, link the new scope for later variable resolution
            if (stack.size() > 1) {
                ScopeNode currentScope = (ScopeNode) stack.peek();
                ScopeNode previousScope = (ScopeNode) stack.peek(1);
                currentScope.setParentScope(previousScope);
            }
        }

        TranslatedPrinter printer = new TranslatedPrinter();
        stack.peek().traverse(printer);
        return printer.toString();
    }



    public static void translate(Node root) {
//        root.traverse(new FlattenNestedRuleSets());
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("You must specify at least one input file.");
            System.exit(1);
        }

        LessTranslator translator = new LessTranslator();

        List<File> files = new ArrayList<File>();
        for (String fileName : args) {
            files.add(new File(fileName));
        }

        try {
            System.out.println(translator.translateFiles(files));
        } catch (IOException e) {
            System.err.println("Unable to read input file.");
        }
    }

    /*private class TranslationUnit {
        private final String _input;
        private String _path;

        public TranslationUnit(File file) throws IOException {
            this(new FileInputStream(file));
            _path = file.getAbsolutePath();
        }

        public TranslationUnit(InputStream stream) throws IOException {
            this(IOUtils.toString(stream, "UTF-8"));
            _path = null;
        }

        public TranslationUnit(String input) {
            _input = input;
            _path = null;
        }

        public String getInput() {
            return _input;
        }

        public String getPath() {
            return _path;
        }
    }*/
}
