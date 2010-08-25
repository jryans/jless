package com.bazaarvoice.jless.ast.util;

import java.util.ListIterator;

/**
 * Extends {@link ListIterator} to allow mutation in arbitrary places within
 * the list, while still keep the active iterator in sync with the state of the
 * list.
 */
public interface RandomAccessListIterator<T> extends ListIterator<T> {

    T remove(int index);

    void set(int index, T child);

    void add(int index, T child);
}
