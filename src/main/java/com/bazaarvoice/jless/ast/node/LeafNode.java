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

package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

import java.util.Collections;
import java.util.List;

/**
 * Super class for all leaf nodes of the tree.
 */
public abstract class LeafNode extends Node {

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

    protected abstract boolean visit(NodeNavigationVisitor visit);

    @Override
    public final boolean filter(NodeNavigationVisitor visitor) {
        return visit(visitor);
    }

    @Override
    public final boolean traverse(NodeNavigationVisitor visitor) {
        return (!isVisible() && !visitInvisible(visitor)) || visit(visitor);
    }
}
