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

import com.bazaarvoice.jless.ast.util.NodeTreeUtils;
import com.bazaarvoice.jless.ast.visitor.InclusiveNodeVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class SelectorNode extends InternalNode {

    public SelectorNode() {
        super();
        setAdditionVisitor();
    }

    public SelectorNode(Node child) {
        this();
        addChild(child);
    }

    private void setAdditionVisitor() {
        setAdditionVisitor(new InclusiveNodeVisitor() {
            /**
             * Absorb all children of the given selector. This assumes that cloning is not necessary.
             */
            @Override
            public boolean add(SelectorNode selector) {
                // If the first segment is a universal ("*") segment, it must remain first in the list
                SelectorSegmentNode sourceSegment = NodeTreeUtils.getFirstChild(selector, SelectorSegmentNode.class);
                if (sourceSegment.isUniversal()) {
                    // If the first destination segment is not a sub-element selector and it has no combinator,
                    // switch it to the descendant combinator before absorbing the universal segment.
                    setCombinatorIfNotSubElement(SelectorNode.this);
                    // Add the universal HTML segment at the front of the list
                    addChild(0, sourceSegment);
                }

                // If the first source segment is not a sub-element selector and it has no combinator,
                // switch it to the descendant combinator before absorbing the segments.
                setCombinatorIfNotSubElement(selector);

                NodeTreeUtils.moveChildren(selector, SelectorNode.this);
                return false; // Don't add the original selector itself
            }
        });
    }

    private void setCombinatorIfNotSubElement(SelectorNode selector) {
        SelectorSegmentNode sourceSegment = NodeTreeUtils.getFirstChild(selector, SelectorSegmentNode.class);
        if (!sourceSegment.isSubElementSelector() && sourceSegment.getCombinator().equals(SelectorSegmentNode.NO_COMBINATOR)) {
            sourceSegment.setCombinator(SelectorSegmentNode.DESCENDANT_COMBINATOR);
        }
    }

    @Override
    protected boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    protected boolean enter(NodeNavigationVisitor visitor) {
        return visitor.enter(this);
    }

    @Override
    protected boolean exit(NodeNavigationVisitor visitor) {
        return visitor.exit(this);
    }

    @Override
    protected boolean visitInvisible(NodeNavigationVisitor visitor) {
        return visitor.visitInvisible(this);
    }

    /**
     * The internal state of this node will be rebuilt as cloned children are added by the super class.
     */
    @Override
    public SelectorNode clone() {
        return (SelectorNode) super.clone();
    }

    /**
     * Recreate internal state before children are cloned.
     */
    @Override
    protected void cloneChildren(InternalNode node) {
        SelectorNode selector = (SelectorNode) node;

        // Reset internal state
        selector.setAdditionVisitor();

        super.cloneChildren(node);
    }
}
