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
import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeNavigationVisitor;

public class VariableReferenceNode extends LeafNode {

    private String _name;

    public VariableReferenceNode(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    /**
     * Search up the scope tree to locate the variable's value. The parser has already verified that
     * the variable is defined.
     */
    public String getValue() {
        for (ScopeNode scope = NodeTreeUtils.getParentScope(this); scope != null; scope = NodeTreeUtils.getParentScope(scope)) {
            ExpressionGroupNode value = scope.getVariable(_name);
            if (value == null) {
                continue;
            }

            return value.toString();
        }

        throw new IllegalStateException("The variable " + _name + " could not be found in any parent scope.");
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
