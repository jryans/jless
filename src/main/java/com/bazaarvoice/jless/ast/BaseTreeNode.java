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
import com.google.common.collect.ForwardingListIterator;
import org.parboiled.trees.MutableTreeNode;
import org.parboiled.trees.TreeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * A base implementation of the {@link org.parboiled.trees.MutableTreeNode}.
 *
 * @param <T> the actual implementation type of this MutableTreeNodeImpl
 */
public class BaseTreeNode<T extends BaseTreeNode<T>> implements MutableTreeNode<T> {

    private final List<T> _children = new ArrayList<T>();
    private final List<T> _childrenView = Collections.unmodifiableList(_children);
    private RandomAccessListIterator<T> _childIterator;
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
        getChildIterator().add(index, child);
    }

    @Override
    public void setChild(int index, T child) {
        getChildIterator().set(index, child);
    }

    @Override
    public T removeChild(int index) {
        return getChildIterator().remove(index);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BaseTreeNode<T>> void setParent(T node, BaseTreeNode<T> parent) {
        if (node != null) {
            ((BaseTreeNode) node)._parent = parent;
        }
    }

    /*public ListIterator<T> getChildIterator() {
        if (_childIterator == null) {
            _childIterator = new ForwardingListIterator<T>() {
                private ListIterator<T> delegate = _children.listIterator();

                @Override
                protected ListIterator<T> delegate() {
                    return delegate;
                }

                @Override
                public void add(T child) {
                    // detach new child from old parent
                    if (child != null) {
                        if (child.getParent() == _outer) return;
                        if (child.getParent() != null) {
                            TreeUtils.removeChild(child.getParent(), child);
                        }
                    }

                    // attach new child
                    delegate().add(child);
                    setParent(child, _outer);
                }
            };
        }

        return _childIterator;
    }*/

    public RandomAccessListIterator<T> getChildIterator() {
        if (_childIterator == null) {
            return newChildIterator();
        }

        return _childIterator;
    }

    public RandomAccessListIterator<T> newChildIterator() {
        return _childIterator = new MutableChildIterator();
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
        public int nextIndex() {
            return _cursor;
        }

        @Override
        public int previousIndex() {
            return _cursor - 1;
        }

        @Override
        public void remove() {
            remove(_lastReturned);
        }

        @Override
        public T remove(int index) {
            Preconditions.checkElementIndex(index, _children.size());
            T removed = _children.remove(index);
            setParent(removed, null);

            // update cursor
            if (index < _cursor) {
                _cursor--;
            }
            _lastReturned = -1;

            return removed;
        }

        @Override
        public void set(T child) {
            set(_lastReturned, child);
        }

        @Override
        public void set(int index, T child) {
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
        public void add(T child) {
            add(_cursor, child);
        }

        @Override
        public void add(int index, T child) {
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

            // update cursor
            if (index <= _cursor) {
                _cursor++;
            }
            _lastReturned = -1;
        }
    }
}