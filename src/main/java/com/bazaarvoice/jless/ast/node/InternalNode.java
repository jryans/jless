package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.util.RandomAccessListIterator;
import com.bazaarvoice.jless.ast.visitor.InclusiveNodeVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;
import com.google.common.base.Preconditions;
import org.parboiled.trees.TreeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

/**
 * Super class for all internal nodes of the tree.
 *
 * A variety of methods allow for manipulating the children of an InternalNode, while still ensuring that
 * only one parent owns a given child node at any one time. A mutable iterator is available, allowing for
 * typical {@link ListIterator} operations, as well as modifications at arbitrary indices from the iterator.
 * Additionally, the child set may be modified while iteration is occurring without going through the iterator
 * directly. 
 */
public abstract class InternalNode extends Node {

    private List<Node> _children = new ArrayList<Node>();
    private List<Node> _childrenView = Collections.unmodifiableList(_children);
    private Stack<MutableChildIterator> _childIteratorStack = new Stack<MutableChildIterator>();
    private NodeAdditionVisitor _additionVisitor = InclusiveNodeVisitor.getInstance();

    public InternalNode() {
    }

    public InternalNode(Node node) {
        addChild(node);
    }

    @Override
    public List<Node> getChildren() {
        return _childrenView;
    }

    /**
     * Registers a visitor that will be called each time a child node is about to be added.
     * If the visitor returns false from such a call, the child is not added. 
     * @see com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor
     */
    protected void setAdditionVisitor(NodeAdditionVisitor additionVisitor) {
        _additionVisitor = additionVisitor;
    }

    // ********** Modification **********

    @Override
    public boolean addChild(Node child) {
        addChild(_children.size(), child);
        return true;
    }

    public void addChildren(Collection<? extends Node> children) {
        for (Node child : children) {
            addChild(child);
        }
    }

    @Override
    public void addChild(int index, Node child) {
        Preconditions.checkElementIndex(index, _children.size() + 1);

        // ignore empty nodes
        if (child == null || !child.hasData()) {
            return;
        }

        // check addition visitor
        if (!child.add(_additionVisitor)) {
            return;
        }

        // detach new child from old parent
        InternalNode parent = child.getParent();
        if (parent == this) {
            return;
        }
        if (parent != null) {
            TreeUtils.removeChild(parent, child);
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
        if (child == null) {
            return;
        }

        // detach old child
        Node old = _children.get(index);
        if (old == child) {
            return;
        }
        old.setParent(null);

        // detach new child from old parent
        InternalNode parent = child.getParent();
        if (parent != null && parent != this) {
            TreeUtils.removeChild(parent, child);
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

    public void clearChildren() {
        while (_children.size() > 0) {
            removeChild(0);
        }
    }

    // ********** Iteration **********

    public boolean isIterating() {
        return !_childIteratorStack.isEmpty();
    }

    public RandomAccessListIterator<Node> getLatestChildIterator() {
        Preconditions.checkState(isIterating(), "There are no child iterators.");

        return _childIteratorStack.peek();
    }

    public RandomAccessListIterator<Node> pushChildIterator() {
        MutableChildIterator it = new MutableChildIterator();

        _childIteratorStack.push(it);

        return it;
    }

    public void popChildIterator() {
        _childIteratorStack.pop();
    }

    // ********** Visitors **********

    protected abstract boolean enter(NodeNavigationVisitor visitor);

    protected abstract boolean exit(NodeNavigationVisitor visitor);

    @Override
    public final boolean filter(NodeNavigationVisitor visitor) {
        if (enter(visitor)) {
            ListIterator<Node> it = pushChildIterator();
            while (it.hasNext()) {
                Node child = it.next();
                if (!child.filter(visitor)) {
                    it.remove();
                }
            }
            popChildIterator();
        }

        return exit(visitor);
    }

    @Override
    public final boolean traverse(NodeNavigationVisitor visitor) {
        if (!isVisible() && !visitInvisible(visitor)) {
            return true;
        }

        if (enter(visitor)) {
            ListIterator<Node> it = pushChildIterator();
            while (it.hasNext()) {
                Node child = it.next();
                if (!child.traverse(visitor)) {
                    break;
                }
            }
            popChildIterator();
        }

        return exit(visitor);
    }

    // ********** Cloning **********

    /**
     * Clones fields and child nodes, but ignores the iterator stack (since the new nodes are not attached
     * to the tree and so should not be part of any kind of iteration).
     */
    @Override
    public InternalNode clone() {
        InternalNode node = (InternalNode) super.clone();

        // Reset internal state
        node._children = new ArrayList<Node>();
        node._childrenView = Collections.unmodifiableList(node._children);
        node._childIteratorStack = new Stack<MutableChildIterator>();

        cloneChildren(node);

        return node;
    }

    protected void cloneChildren(InternalNode node) {
        // Copy all children
        for (Node child : _children) {
            TreeUtils.addChild(node, child.clone());
        }
    }

    private class MutableChildIterator implements RandomAccessListIterator<Node> {

        private int _cursor = 0;
        private int _lastReturned = -1;

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
        public Node remove(int relativeIndex) {
            return removeChild(_cursor + relativeIndex);
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
        public void set(int relativeIndex, Node child) {
            setChild(_cursor + relativeIndex, child);
        }

        @Override
        public void add(Node child) {
            addChild(_cursor, child);
        }

        @Override
        public void add(int relativeIndex, Node child) {
            addChild(_cursor + relativeIndex, child);
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