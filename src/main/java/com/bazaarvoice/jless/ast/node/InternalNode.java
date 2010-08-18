/*
 * Copyright (C) 2009 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.util.RandomAccessListIterator;
import com.bazaarvoice.jless.ast.visitor.InclusiveNodeVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;
import com.google.common.base.Preconditions;
import org.parboiled.trees.TreeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

/**
 * A base implementation of the {@link org.parboiled.trees.MutableTreeNode}.
 *
 * TODO: Rewrite this!
 *
 * @param <T> the actual implementation type of this MutableTreeNodeImpl
 */
public abstract class InternalNode extends Node {

    private List<Node> _children = new ArrayList<Node>();
    private List<Node> _childrenView = Collections.unmodifiableList(_children);
    private Stack<MutableChildIterator> _childIteratorStack = new Stack<MutableChildIterator>();
    protected NodeAdditionVisitor _additionVisitor = InclusiveNodeVisitor.getInstance();

    public InternalNode() {
    }

    public InternalNode(Node node) {
        addChild(node);
    }

    @Override
    public List<Node> getChildren() {
        return _childrenView;
    }

    /*public <R extends T> List<R> getChildren(Class<R> clazz) {
        List<R> filteredChildren = new ArrayList<R>();

        for (T child : _childrenView) {
            if (clazz.isInstance(child)) {
                //noinspection unchecked
                filteredChildren.add((R) child);
            }
        }

        return filteredChildren;
    }

    public <R extends T> R getFirstChild(Class<R> clazz) {
        for (T child : _childrenView) {
            if (clazz.isInstance(child)) {
                //noinspection unchecked
                return (R) child;
            }
        }

        return null;
    }*/

    @Override
    public boolean addChild(Node child) {
        addChild(_children.size(), child);
        return true;
    }

    @Override
    public void addChild(int index, Node child) {
        Preconditions.checkElementIndex(index, _children.size() + 1);

        // ignore empty nodes
        if (child == null || !child.hasData()) {
            return;
        }

        // check addition visitor
        if (!_additionVisitor.add(child)) {
            return;
        }

        // detach new child from old parent
        if (child.getParent() == this) return;
        if (child.getParent() != null) {
            TreeUtils.removeChild(child.getParent(), child);
        }

        // attach new child
        _children.add(index, child);
        child.setParent(this);

        // notify iterators
        for (MutableChildIterator it : _childIteratorStack) {
            it.addEvent(index, child);
        }
    }

    @Override
    public void setChild(int index, Node child) {
        Preconditions.checkElementIndex(index, _children.size());

        // ignore empty nodes
        if (child == null || !child.hasData()) {
            return;
        }

        // detach old child
        Node old = _children.get(index);
        if (old == child) return;
        old.setParent(null);

        // detach new child from old parent
        if (child.getParent() != this) {
            TreeUtils.removeChild(child.getParent(), child);
        }

        // attach new child
        _children.set(index, child);
        child.setParent(this);
    }

    @Override
    public Node removeChild(int index) {
        // remove and detach child
        Preconditions.checkElementIndex(index, _children.size());
        Node removed = _children.remove(index);
        removed.setParent(null);

        // notify iterators
        for (MutableChildIterator it : _childIteratorStack) {
            it.removeEvent(index);
        }

        return removed;
    }

    public RandomAccessListIterator<Node> getLatestChildIterator() {
        Preconditions.checkState(!_childIteratorStack.isEmpty(), "There are no child iterators.");

        return _childIteratorStack.peek();
    }

    public RandomAccessListIterator<Node> pushChildIterator() {
        MutableChildIterator it = new MutableChildIterator();

        _childIteratorStack.push(it);

        return it;
    }

    public RandomAccessListIterator<Node> pushChildIterator(int startPosition) {
        MutableChildIterator it = new MutableChildIterator(startPosition);

        _childIteratorStack.push(it);
        
        return it;
    }

    public void popChildIterator() {
        _childIteratorStack.pop();
    }

    @Override
    public boolean accept(NodeTraversalVisitor visitor) {
        if (visitor.enter(this)) {
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

    /**
     * Clones fields and child nodes, but ignores the iterator stack (since the new nodes are not attached
     * to the tree and so should not be part of any kind of iteration).
     */
    @SuppressWarnings ({"unchecked", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    public InternalNode clone() {
        // Clone any primitive fields
        InternalNode node = (InternalNode) super.clone();

        // Reset internal state
        node._children = new ArrayList<Node>();
        node._childrenView = Collections.unmodifiableList(node._children);
        node._childIteratorStack = new Stack<MutableChildIterator>();

        // Copy all children
        for (Node child : _children) {
            TreeUtils.addChild(node, child.clone());
        }

        return node;
    }

    private class MutableChildIterator implements RandomAccessListIterator<Node> {

        private int _cursor;
        private int _lastReturned = -1;

        public MutableChildIterator() {
            this(0);
        }

        public MutableChildIterator(int startPosition) {
            _cursor = startPosition;
        }

        @Override
        public boolean hasNext() {
            return _cursor != _children.size();
        }

        @Override
        public Node next() {
            Preconditions.checkPositionIndex(_cursor + 1, _children.size());

            _lastReturned = _cursor;

            return _children.get(_cursor++);
        }

        @Override
        public Node peekNext() {
            return _children.get(_cursor);
        }

        @Override
        public boolean hasPrevious() {
            return _cursor != 0;
        }

        @Override
        public Node previous() {
            Preconditions.checkPositionIndex(_cursor - 1, _children.size());

            _lastReturned = --_cursor;

            return _children.get(_cursor);
        }

        @Override
        public Node peekPrevious() {
            return _children.get(_cursor - 1);
        }

        @Override
        public int nextIndex() {
            return _cursor;
        }

        @Override
        public int previousIndex() {
            return _cursor - 1;
        }

        @Override
        public void remove() {
            removeChild(_lastReturned);
        }

        @Override
        public Node remove(int index) {
            return removeChild(index);
        }

        private void removeEvent(int index) {
            // update cursor
            if (index < _cursor) {
                _cursor--;
            }
            _lastReturned = -1;
        }

        @Override
        public void set(Node child) {
            setChild(_lastReturned, child);
        }

        @Override
        public void set(int index, Node child) {
            setChild(index, child);
        }

        @Override
        public void add(Node child) {
            addChild(_cursor, child);
        }

        @Override
        public void add(int index, Node child) {
            addChild(index, child);
        }   

        private void addEvent(int index, Node child) {
            // update cursor
            if (index <= _cursor) {
                _cursor++;
            }
            _lastReturned = -1;
        }
    }
}