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

package com.bazaarvoice.jless.ast;

import com.google.common.base.Preconditions;
import org.parboiled.trees.MutableTreeNode;
import org.parboiled.trees.TreeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * A base implementation of the {@link org.parboiled.trees.MutableTreeNode}.
 *
 * TODO: Add leaf node class
 *
 * @param <T> the actual implementation type of this MutableTreeNodeImpl
 */
public abstract class BaseTreeNode<T extends BaseTreeNode<T>> implements MutableTreeNode<T>, Cloneable {

    private List<T> _children = new ArrayList<T>();
    private List<T> _childrenView = Collections.unmodifiableList(_children);
    private Stack<MutableChildIterator> _childIteratorStack = new Stack<MutableChildIterator>();
    private T _parent;

    @Override
    public T getParent() {
        return _parent;
    }

    @Override
    public List<T> getChildren() {
        return _childrenView;
    }

    @Override
    public void addChild(int index, T child) {
        Preconditions.checkElementIndex(index, _children.size() + 1);

        // detach new child from old parent
        if (child != null) {
            if (child.getParent() == BaseTreeNode.this) return;
            if (child.getParent() != null) {
                TreeUtils.removeChild(child.getParent(), child);
            }
        }

        // attach new child
        _children.add(index, child);
        setParent(child, BaseTreeNode.this);

        // notify iterators
        for (MutableChildIterator it : _childIteratorStack) {
            it.addEvent(index, child);
        }
    }

    @Override
    public void setChild(int index, T child) {
        Preconditions.checkElementIndex(index, _children.size());

        // detach old child
        T old = _children.get(index);
        if (old == child) return;
        setParent(old, null);

        // detach new child from old parent
        if (child != null && child.getParent() != BaseTreeNode.this) {
            TreeUtils.removeChild(child.getParent(), child);
        }

        // attach new child
        _children.set(index, child);
        setParent(child, BaseTreeNode.this);
    }

    @Override
    public T removeChild(int index) {
        // remove and detach child
        Preconditions.checkElementIndex(index, _children.size());
        T removed = _children.remove(index);
        setParent(removed, null);

        // notify iterators
        for (MutableChildIterator it : _childIteratorStack) {
            it.removeEvent(index);
        }

        return removed;
    }

    @SuppressWarnings("unchecked")
    private static <T extends BaseTreeNode<T>> void setParent(T node, BaseTreeNode<T> parent) {
        if (node != null) {
            ((BaseTreeNode) node)._parent = parent;
        }
    }

    public RandomAccessListIterator<T> getLatestChildIterator() {
        Preconditions.checkState(!_childIteratorStack.isEmpty(), "There are no child iterators.");

        return _childIteratorStack.peek();
    }

    public RandomAccessListIterator<T> pushChildIterator() {
        MutableChildIterator it = new MutableChildIterator();

        _childIteratorStack.push(it);

        return it;
    }

    public RandomAccessListIterator<T> pushChildIterator(int startPosition) {
        MutableChildIterator it = new MutableChildIterator(startPosition);

        _childIteratorStack.push(it);
        
        return it;
    }

    public void popChildIterator() {
        _childIteratorStack.pop();
    }

    /**
     * Clones fields and child nodes, but ignores the iterator stack (since the new nodes are not attached
     * to the tree and so should not be part of any kind of iteration).
     */
    @SuppressWarnings ({"unchecked", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    public T clone() {
        T node;
        try {
            // Clone any primitive fields
            node = (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Object is marked Cloneable.");
        }

        // Reset internal state
        node._children = new ArrayList<T>();
        node._childrenView = Collections.unmodifiableList(node._children);
        node._childIteratorStack = new Stack<MutableChildIterator>();
        node._parent = null;

        // Copy all children
        for (T child : _children) {
            TreeUtils.addChild(node, child.clone());
        }

        return node;
    }

    private class MutableChildIterator implements RandomAccessListIterator<T> {

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
        public T next() {
            Preconditions.checkPositionIndex(_cursor + 1, _children.size());

            _lastReturned = _cursor;

            return _children.get(_cursor++);
        }

        @Override
        public T peekNext() {
            return _children.get(_cursor);
        }

        @Override
        public boolean hasPrevious() {
            return _cursor != 0;
        }

        @Override
        public T previous() {
            Preconditions.checkPositionIndex(_cursor - 1, _children.size());

            _lastReturned = --_cursor;

            return _children.get(_cursor);
        }

        @Override
        public T peekPrevious() {
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
        public T remove(int index) {
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
        public void set(T child) {
            setChild(_lastReturned, child);
        }

        @Override
        public void set(int index, T child) {
            setChild(index, child);
        }

        @Override
        public void add(T child) {
            addChild(_cursor, child);
        }

        @Override
        public void add(int index, T child) {
            addChild(index, child);
        }

        private void addEvent(int index, T child) {
            // update cursor
            if (index <= _cursor) {
                _cursor++;
            }
            _lastReturned = -1;
        }
    }
}