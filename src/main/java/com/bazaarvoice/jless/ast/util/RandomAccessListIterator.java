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

    /**
     * Copy the current position of the supplied iterator.
     */
    void copy(ListIterator<T> iterator);
}
