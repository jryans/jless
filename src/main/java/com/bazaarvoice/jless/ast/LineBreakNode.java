package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.ast.visitor.NodeVisitor;
import org.apache.commons.lang.StringUtils;

import java.util.ListIterator;

/**
 * Stores only the number of line breaks that occurred in the input text.
 */
public class LineBreakNode extends Node {
    
    private int _lineBreaks;

    public LineBreakNode(String text) {
        super();
        _lineBreaks = StringUtils.countMatches(text, "\n");
    }

    @Override
    protected boolean hasData() {
        return _lineBreaks > 0;
    }

    public int getLineBreaks() {
        return _lineBreaks;
    }

    @Override
    public boolean accept(NodeVisitor visitor) {
        if (visitor.visitEnter(this)) {
            ListIterator<Node> it = pushChildIterator();
            while (it.hasNext()) {
                Node child = it.next();
                if (!child.accept(visitor)) {
                    break;
                }
            }
            popChildIterator();
        }

        return visitor.visit(this);
    }
}
