package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.ArgumentsNode;
import com.bazaarvoice.jless.ast.node.ExpressionGroupNode;
import com.bazaarvoice.jless.ast.node.ExpressionNode;
import com.bazaarvoice.jless.ast.node.ExpressionPhraseNode;
import com.bazaarvoice.jless.ast.node.FilterArgumentNode;
import com.bazaarvoice.jless.ast.node.FunctionNode;
import com.bazaarvoice.jless.ast.node.LineBreakNode;
import com.bazaarvoice.jless.ast.node.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.PropertyNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.node.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.node.SimpleNode;
import com.bazaarvoice.jless.ast.node.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.node.VariableDefinitionNode;
import com.bazaarvoice.jless.ast.node.VariableReferenceNode;
import com.bazaarvoice.jless.ast.util.NodeTreeUtils;

import java.util.List;

public class Printer extends InclusiveNodeVisitor {

    private static final int INDENT_STEP = 4;
    private static final int COMPRESSED_LINE_BREAK_POSITION = 4000;

    private boolean _compress;

    private StringBuilder _sb = new StringBuilder();
    private int _indent = 0;
    private boolean _lastPrintedIndent = false;
    private int _lastCompressedLineBreak = 0;

    public Printer() {
        this(false);
    }

    public Printer(boolean compress) {
        _compress = compress;
    }
    
    // Node output

    @Override
    public boolean enter(ArgumentsNode node) {
        print('(');
        return super.enter(node);
    }

    @Override
    public boolean exit(ArgumentsNode node) {
        print(')');
        return super.exit(node);
    }

    @Override
    public boolean exit(ExpressionGroupNode node) {
        if (NodeTreeUtils.parentHasNext(node)) {
            print(',').printOptional(' ');
        }
        return super.exit(node);
    }

    @Override
    public boolean exit(ExpressionNode node) {
        if (NodeTreeUtils.parentHasNext(node)) {
            print(' ');
        }
        return super.exit(node);
    }

    @Override
    public boolean exit(ExpressionPhraseNode node) {
        if (NodeTreeUtils.parentHasNext(node)) {
            print(',').printOptional(' ');
        }
        return super.exit(node);
    }

    @Override
    public boolean enter(FunctionNode node) {
        print(node.getName());
        return super.enter(node);
    }

    @Override
    public boolean enter(FilterArgumentNode node) {
        print(node.getName()).print('=');
        return super.enter(node);
    }

    @Override
    public boolean exit(FilterArgumentNode node) {
        if (NodeTreeUtils.parentHasNext(node)) {
            print(',').printOptional(' ');
        }
        return super.exit(node);
    }

    @Override
    public boolean visit(LineBreakNode node) {
        if (!_compress) {
            for (int i = 0; i < node.getLineBreaks(); i++) {
                printLine();
            }
            if (node.getLineBreaks() > 0) {
                printIndent();
            }
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        print("/*").print(node.getValue()).print("*/");
        return super.visit(node);
    }

    @Override
    public boolean enter(PropertyNode node) {
        print(node.getName()).print(':').printOptional(' ');
        return super.enter(node);
    }

    @Override
    public boolean exit(PropertyNode node) {
        if (NodeTreeUtils.parentHasNext(node)) {
            print(";");
        } else {
            printOptional(";");
        }
        if (node.getParent().getChildren().size() > 1 && NodeTreeUtils.parentHasNext(node)) {
            printOptional(' ');
        }
        return super.exit(node);
    }

    @Override
    public boolean enter(RuleSetNode ruleSet) {
        if (_compress) {
            // Check if the inner scope contains nodes
            ScopeNode scope = NodeTreeUtils.getFirstChild(ruleSet, ScopeNode.class);
            return !scope.getChildren().isEmpty();
        }
        return super.enter(ruleSet);
    }

    @Override
    public boolean enter(ScopeNode node) {
        if (node.getParent() != null) {
            printOptional(' ').print('{');
            List<Node> children = node.getChildren();
            if (children.isEmpty()) {
                // do nothing
            } else {
                addIndent();
            }
        }
        return super.enter(node);
    }

    @Override
    public boolean exit(ScopeNode node) {
        if (node.getParent() != null) {
            List<Node> children = node.getChildren();
            if (children.isEmpty()) {
                // do nothing
            } else {
                removeIndent();
            }
            deleteIndent().print('}');

            // Some editors and version control systems don't like extremely long lines, so add
            // line breaks every so often when compressing. 
            if (_compress && _sb.length() - _lastCompressedLineBreak > COMPRESSED_LINE_BREAK_POSITION) {
                printLine();
                _lastCompressedLineBreak = _sb.length();
            }
        }
        return super.exit(node);
    }

    @Override
    public boolean exit(SelectorNode node) {
        if (NodeTreeUtils.parentHasNext(node)) {
            print(',').printOptional(' ');
        }
        return super.exit(node);
    }

    @Override
    public boolean visit(SelectorSegmentNode node) {
        print(node.getCombinator()).print(node.getSimpleSelector());
        return super.visit(node);
    }

    @Override
    public boolean visit(SimpleNode node) {
        print(node.getValue());
        return super.visit(node);
    }

    @Override
    public boolean visit(SingleLineCommentNode node) {
        // C-style single line comments are not part of the CSS spec, so don't print them
        return super.visit(node);
    }

    @Override
    public boolean enter(VariableDefinitionNode node) {
        print(node.getName()).print(": ");
        return super.enter(node);
    }

    @Override
    public boolean exit(VariableDefinitionNode node) {
        print(";");
        if (node.getParent().getChildren().size() > 1 && NodeTreeUtils.parentHasNext(node)) {
            print(' ');
        }
        return super.exit(node);
    }

    @Override
    public boolean visit(VariableReferenceNode node) {
        print(node.getValue());
        return super.visit(node);
    }

    @Override
    public boolean visitInvisible(Node node) {
        visit(new LineBreakNode(node.toString()));
        return false;
    }

    // Printing methods

    private Printer print(String s) {
        _sb.append(s);
        _lastPrintedIndent = false;
        return this;
    }

    private Printer print(Character c) {
        _sb.append(c);
        _lastPrintedIndent = false;
        return this;
    }

    private Printer printOptional(String s) {
        if (!_compress) {
            print(s);
        }
        return this;
    }

    private Printer printOptional(Character c) {
        if (!_compress) {
            print(c);
        }
        return this;
    }

    private Printer printIndent() {
        for (int i = 0; i < _indent; i++) {
            _sb.append(' ');
        }
        _lastPrintedIndent = true;
        return this;
    }

    private Printer printLine() {
        _sb.append('\n');
        _lastPrintedIndent = false;
        return this;
    }

    private Printer addIndent() {
        _indent += INDENT_STEP;
        return this;
    }

    private Printer removeIndent() {
        _indent -= INDENT_STEP;
        return this;
    }

    private Printer deleteIndent() {
        if (_lastPrintedIndent) {
            _sb.delete(_sb.length() - INDENT_STEP, _sb.length());
        }
        return this;
    }

    @Override
    public String toString() {
        return _sb.toString();
    }
}
