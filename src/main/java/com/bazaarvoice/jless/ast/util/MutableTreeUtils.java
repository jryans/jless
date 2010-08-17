package com.bazaarvoice.jless.ast.util;

import com.bazaarvoice.jless.ast.Node;
import com.bazaarvoice.jless.ast.RandomAccessListIterator;
import com.google.common.base.Preconditions;

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
        Node parent = node.getParent();

        Preconditions.checkNotNull(parent);

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
        Node parent = node.getParent();

        Preconditions.checkNotNull(parent);

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
        return node.getParent().getLatestChildIterator().hasNext();
    }

}
