package com.bazaarvoice.jless.ast.util;

import com.bazaarvoice.jless.ast.node.InternalNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ScopeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * TODO: Fix naming scheme
 *
 * Utils for BaseTreeNode nodes? Maybe just for visitors?
 */
public final class MutableTreeUtils {

    private MutableTreeUtils() {}

    /**
     * Add the input node after the current node in its parent's list of children.
     */
    public static void addSiblingAfter(Node node, Node sibling) {
        InternalNode parent = node.getParent();

        // Add the sibling node after the current node
        RandomAccessListIterator<Node> childIterator = parent.getLatestChildIterator();
        childIterator.add(sibling);

        // Rewind the iterator so that the added node is visited
        childIterator.previous();
    }

    /**
     * Add the input nodes after the current node in its parent's list of children.
     */
    public static void addSiblingAfter(Node node, Node... siblings) {
        InternalNode parent = node.getParent();

        // Add the sibling nodes after the current node (also the parent iterator's current node)
        RandomAccessListIterator<Node> childIterator = parent.getLatestChildIterator();
        for (Node sibling : siblings) {
            childIterator.add(sibling);
        }

        // Rewind the iterator so that the added nodes are visited
        for (Node sibling : siblings) {
            childIterator.previous();
        }
    }

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

    public static <C extends Node> List<C> getChildren(InternalNode parent, Class<C> clazz) {
        List<C> filteredChildren = new ArrayList<C>();

        for (Node child : parent.getChildren()) {
            if (clazz.isInstance(child)) {
                //noinspection unchecked
                filteredChildren.add((C) child);
            }
        }

        return filteredChildren;
    }

    public static <C extends Node> C getFirstChild(InternalNode parent, Class<C> clazz) {
        for (Node child : parent.getChildren()) {
            if (clazz.isInstance(child)) {
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
