package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;
import org.apache.commons.lang.StringUtils;

/**
 * Stores only the number of line breaks that occurred in the input text.
 */
public class LineBreakNode extends LeafNode {
    
    private int _lineBreaks;

    public LineBreakNode(String text) {
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
    public boolean traverse(NodeTraversalVisitor visitor) {
        return visitor.visit(this);
    }
}
