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
            public boolean add(SelectorNode node) {
                // If the first segment is not a sub-element selector and it has no combinator,
                // switch it to the descendant combinator before absorbing the segments.
                SelectorSegmentNode segment = NodeTreeUtils.getFirstChild(node, SelectorSegmentNode.class);
                if (!segment.isSubElementSelector() && segment.getCombinator().equals(SelectorSegmentNode.NO_COMBINATOR)) {
                    segment.setCombinator(SelectorSegmentNode.DESCENDANT_COMBINATOR);
                }

                NodeTreeUtils.moveChildren(node, SelectorNode.this);
                return false; // Don't add the original selector itself
            }
        });
    }

    @Override
    public boolean add(NodeAdditionVisitor visitor) {
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
