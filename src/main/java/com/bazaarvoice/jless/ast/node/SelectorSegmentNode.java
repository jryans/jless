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

import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class SelectorSegmentNode extends LeafNode {

    public static final String NO_COMBINATOR = "";
    public static final String DESCENDANT_COMBINATOR = " ";

    private String _combinator;
    private String _simpleSelector;
    private boolean _subElementSelector;
    private boolean _universal;

    public SelectorSegmentNode(String combinator) {
        _combinator = combinator;
    }

    public SelectorSegmentNode(String combinator, String simpleSelector) {
        _combinator = combinator;
        _simpleSelector = simpleSelector;
    }

    public String getCombinator() {
        return _combinator;
    }

    public boolean setCombinator(String combinator) {
        _combinator = combinator;
        return true;
    }

    public String getSimpleSelector() {
        return _simpleSelector;
    }

    public boolean setSimpleSelector(String simpleSelector) {
        _simpleSelector = simpleSelector;
        return true;
    }

    /**
     * Implies that the simple selector starts with some sub-element selection, such as an attribute or pseudo-class.
     */
    public boolean isSubElementSelector() {
        return _subElementSelector;
    }

    public boolean setSubElementSelector(boolean subElementSelector) {
        _subElementSelector = subElementSelector;
        return true;
    }

    public boolean isUniversal() {
        return _universal;
    }

    public boolean setUniversal(boolean universal) {
        _universal = universal;
        return true;
    }

    @Override
    protected boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    protected boolean visit(NodeNavigationVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    protected boolean visitInvisible(NodeNavigationVisitor visitor) {
        return visitor.visitInvisible(this);
    }
}
