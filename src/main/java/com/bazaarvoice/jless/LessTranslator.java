package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.parser.Parser;
import org.apache.commons.io.IOUtils;
import org.parboiled.Parboiled;
import org.parboiled.ParseRunner;
import org.parboiled.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    public String translate(List<File> inputFileList) {
        ParseRunner 
        for ()
    }

    public String translate(File inputFile) throws IOException {
        return translate(IOUtils.toString(new FileInputStream(inputFile), "UTF-8"));
    }

    public ParsingResult<Node> translate(String input) {
        ReportingParseRunner.run(_parser.Document(), input);



        return


    }

    public static void translate(Node root) {
//        root.traverse(new FlattenNestedRuleSets());
    }
}
