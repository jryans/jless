package com.bazaarvoice.jless.ast.util;

import com.bazaarvoice.jless.ast.node.InternalNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ScopeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Utilities for traversing and manipulating the node tree.
 */
public final class NodeTreeUtils {

    private NodeTreeUtils() {}


    public static boolean parentHasNext(Node node) {
        InternalNode parent = node.getParent();
        return parent.isIterating() && parent.getLatestChildIterator().hasNext();
    }

    public static ScopeNode getParentScope(Node node) {
        if (node instanceof ScopeNode) {
            ScopeNode parentScope = ((ScopeNode) node).getParentScope();
            if (parentScope != null) {
                return parentScope;
            }
        }

        for (Node current = node.getParent(); current != null; current = current.getParent()) {
            if (current instanceof ScopeNode) {
                return (ScopeNode) current;
            }
        }

        return null;
    }

    public static <C extends Node> List<C> getChildren(InternalNode parent, Class<C> targetClass) {
        List<C> filteredChildren = new ArrayList<C>();

        for (Node child : parent.getChildren()) {
            if (targetClass.isInstance(child)) {
                //noinspection unchecked
                filteredChildren.add((C) child);
            }
        }

        return filteredChildren;
    }

    public static <C extends Node> C getFirstChild(InternalNode parent, Class<C> targetClass) {
        for (Node child : parent.getChildren()) {
            if (targetClass.isInstance(child)) {
                //noinspection unchecked
                return (C) child;
            }
        }

        return null;
    }

    public static void moveChildren(InternalNode source, InternalNode destination) {
        ListIterator<Node> it = source.pushChildIterator();
        while (it.hasNext()) {
            Node child = it.next();
            destination.addChild(child);
        }
        source.popChildIterator();
    }
}
