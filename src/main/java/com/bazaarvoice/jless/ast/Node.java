package com.bazaarvoice.jless.ast;

import com.bazaarvoice.jless.print.Printer;
import org.parboiled.trees.MutableTreeNodeImpl;
import org.parboiled.trees.TreeUtils;

public class Node extends MutableTreeNodeImpl<Node> {

    public Node() {
        // empty constructor
    }

    public Node(Node child) {
        addChild(child);
    }

    public boolean addChild(Node child) {
        if (child != null) {
            TreeUtils.addChild(this, child);
        }
        return true;
    }

    public void print(Printer printer) {
        printer.append("Yay!");
        printer.printChildren(this);
    }
}