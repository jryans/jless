package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.visitor.FlattenNestedRuleSets;
import com.bazaarvoice.jless.ast.visitor.Printer;
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
 * LessProcessor is JLESS's parsing and translation engine that is used to convert input files that
 * use the <a href="http://lesscss.org/">LESS</a> styling framework syntax into standard CSS.
 * This implementation is based on the Ruby version of LESS by Alexis Sellier.
 *
 * At this time, the following LESS features are supported:
 * <ul>
 *   <li>Variables</li>
 *   <li>Mixins</li>
 *   <li>Mixin Arguments</li>
 *   <li>Nesting</li>
 * </ul>
 *
 * The following LESS features are not currently supported:
 * <ul>
 *   <li>Operations</li>
 *   <li>Accessors</li>
 *   <li>Imports</li>
 * </ul>
 *
 * This implementation does not attempt to generate the same output as the Ruby version.
 * Translation differences from LESS Ruby include:
 * <ul>
 *   <li>Equivalent adjacent rule sets are not grouped</li>
 *   <li>Empty rule sets are preserved</li>
 *   <li>Numbers and colors are not reformatted</li>
 * </ul>
 *
 * This list only notes changes in the <em>translation</em> stage. See {@link com.bazaarvoice.jless.parser.Parser} for details
 * on any changes to the <em>parsing</em> stage.
 *
 * <strong>This class is not thread-safe.</strong> Clients must ensure that multiple threads do not attempt to use the same
 * instance of LessProcessor at the same time.
 *
 * @see com.bazaarvoice.jless.parser.Parser
 */
public class LessProcessor {

    // Controls whether only parsing or both parsing and translation are performed.
    private boolean _translationEnabled;
    private Parser _parser;

    public LessProcessor() {
        this(true);
    }

    public LessProcessor(boolean translationEnabled) {
        _translationEnabled = translationEnabled;
        _parser = Parboiled.createParser(Parser.class, _translationEnabled);
    }

    public String process(File file) throws IOException {
        return processFiles(Collections.singletonList(file));
    }

    public String process(InputStream stream) throws IOException {
        return processStreams(Collections.singletonList(stream));
    }

    public String process(String input) {
        return processStrings(Collections.singletonList(input));
    }

    public String processFiles(List<File> files) throws IOException {
        List<InputStream> streams = new ArrayList<InputStream>();
        for (File file : files) {
            streams.add(new FileInputStream(file));
        }
        return processStreams(streams);
    }

    public String processStreams(List<InputStream> streams) throws IOException {
        List<String> strings = new ArrayList<String>();
        for (InputStream stream : streams) {
            strings.add(IOUtils.toString(stream, "UTF-8"));
        }
        return processStrings(strings);
    }

    /**
     * Each input is processed individually, creating a {@link ScopeNode} on the parser's {@link ValueStack}.
     * These ScopeNodes are then joined together to allow for variable resolution across scopes. The resulting
     * output consists of the translated version of the last input file. This means that all inputs serve as
     * silent context files, but will not produce any output of their own.
     */
    public String processStrings(List<String> inputs) {
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

        // Collect all nodes that form the output
        Node output = stack.peek();

        // Perform additional translation steps if needed
        if (_translationEnabled) {
            output.traverse(new FlattenNestedRuleSets());
        }

        // Print the output nodes
        Printer printer = new Printer();
        output.traverse(printer);
        return printer.toString();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("You must specify at least one input file.");
            System.exit(1);
        }

        LessProcessor translator = new LessProcessor();

        List<File> files = new ArrayList<File>();
        for (String fileName : args) {
            files.add(new File(fileName));
        }

        try {
            System.out.println(translator.processFiles(files));
        } catch (IOException e) {
            System.err.println("Unable to read input file.");
        }
    }
}
