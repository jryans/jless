package com.bazaarvoice.jless.ast.util;

import java.util.ListIterator;

/**
 * Extends {@link ListIterator} to allow mutation in arbitrary places within
 * the list, while still keep the active iterator in sync with the state of the
 * list.
 */
public interface RandomAccessListIterator<T> extends ListIterator<T> {

    /**
     * Remove an element at the position {@link #nextIndex} plus relativeIndex.
     */
    T remove(int relativeIndex);

    /**
     * Replace an element at the position {@link #nextIndex} plus relativeIndex.
     */
    void set(int relativeIndex, T child);

    /**
     * Add an element at the position {@link #nextIndex} plus relativeIndex.
     */
    void add(int relativeIndex, T child);
}
