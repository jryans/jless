/**
 * Copyright 2010 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author J. Ryan Stinnett (ryan.stinnett@bazaarvoice.com)
 */

package com.bazaarvoice.jless.ast.util;

import com.bazaarvoice.jless.ast.node.InternalNode;
import com.bazaarvoice.jless.ast.node.LineBreakNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SpacingNode;
import com.bazaarvoice.jless.ast.visitor.InclusiveNodeVisitor;

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

    public static boolean parentHasAnyFollowing(Node node, Class targetClass) {
        InternalNode parent = node.getParent();

        if (!parent.isIterating()) {
            return false;
        }

        boolean found = false;

        RandomAccessListIterator<Node> it = parent.pushChildIterator(true);

        // Search ahead for an instance of the target class
        while (it.hasNext()) {
            if (targetClass.isInstance(it.next())) {
                found = true;
                break;
            }
        }

        return found;
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

    public static List<Node> getChildrenWithVisibility(InternalNode parent, boolean visibility) {
        List<Node> filteredChildren = new ArrayList<Node>();

        for (Node child : parent.getChildren()) {
            if (child.isVisible() == visibility) {
                //noinspection unchecked
                filteredChildren.add(child);
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

    public static InternalNode filterWhiteSpace(InternalNode node) {
        node.filter(new InclusiveNodeVisitor() {
            @Override
            public boolean visit(LineBreakNode node) {
                return false;
            }

            @Override
            public boolean visit(SpacingNode node) {
                return false;
            }
        });
        return node;
    }
}
