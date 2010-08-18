package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class LeafNode extends Node {

    @Override
    public boolean addChild(Node child) {
        throw new UnsupportedOperationException("Leaf nodes can't contain children.");
    }

    @Override
    public void addChild(int index, Node child) {
        throw new UnsupportedOperationException("Leaf nodes can't contain children.");
    }

    @Override
    public void setChild(int index, Node child) {
        throw new UnsupportedOperationException("Leaf nodes can't contain children.");
    }

    @Override
    public Node removeChild(int index) {
        throw new UnsupportedOperationException("Leaf nodes can't contain children.");
    }

    @Override
    public List<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public boolean accept(NodeTraversalVisitor visitor) {
        return visitor.visit(this);
    }

    public boolean acceptReflect(NodeTraversalVisitor visitor) {
        try {
            Method visitMethod = visitor.getClass().getMethod("visit", getClass());

            try {
                visitMethod.setAccessible(true);
                return (Boolean) visitMethod.invoke(visitor, this);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Visit method invocation failed!", e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Visit method access failed!", e);
            }

        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Visit method could not be found!", e);
        }
    }
}
