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

package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.ArgumentsNode;
import com.bazaarvoice.jless.ast.node.ExpressionGroupNode;
import com.bazaarvoice.jless.ast.node.ExpressionNode;
import com.bazaarvoice.jless.ast.node.ExpressionPhraseNode;
import com.bazaarvoice.jless.ast.node.FilterArgumentNode;
import com.bazaarvoice.jless.ast.node.FunctionNode;
import com.bazaarvoice.jless.ast.node.LineBreakNode;
import com.bazaarvoice.jless.ast.node.MultipleLineCommentNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.ParametersNode;
import com.bazaarvoice.jless.ast.node.PropertyNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorGroupNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.node.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.node.SimpleNode;
import com.bazaarvoice.jless.ast.node.SingleLineCommentNode;
import com.bazaarvoice.jless.ast.node.SpacingNode;
import com.bazaarvoice.jless.ast.node.VariableDefinitionNode;
import com.bazaarvoice.jless.ast.node.VariableReferenceNode;

public abstract class DefaultNodeVisitor implements NodeAdditionVisitor, NodeNavigationVisitor {
    
    protected abstract boolean defaultValue();
    
    // Base visit methods
    // These eliminate the need for visit methods that no visitor uses
    
    @Override
    public boolean add(Node node) {
        return defaultValue();
    }

    @Override
    public boolean enter(Node node) {
        return defaultValue();
    }

    @Override
    public boolean visit(Node node) {
        return defaultValue();
    }

    @Override
    public boolean exit(Node node) {
        return defaultValue();
    }

    @Override
    public boolean visitInvisible(Node node) {
        return defaultValue();
    }

    // Concrete visit methods (NodeAdditionVisitor)

    @Override
    public boolean add(ParametersNode node) {
        return defaultValue();
    }

    @Override
    public boolean add(RuleSetNode node) {
        return defaultValue();
    }

    @Override
    public boolean add(ScopeNode node) {
        return defaultValue();
    }

    @Override
    public boolean add(SelectorNode node) {
        return defaultValue();
    }

    @Override
    public boolean add(VariableDefinitionNode node) {
        return defaultValue();
    }

    // Concrete visit methods (NodeTraversalVisitor)

    @Override
    public boolean enter(ArgumentsNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(ArgumentsNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(ExpressionGroupNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(ExpressionGroupNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(ExpressionNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(ExpressionPhraseNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(FunctionNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(FunctionNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(FilterArgumentNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(FilterArgumentNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(LineBreakNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(MultipleLineCommentNode node) {
        return defaultValue();
    }

    @Override
    public boolean add(PropertyNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(PropertyNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(PropertyNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(RuleSetNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(RuleSetNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(ScopeNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(ScopeNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(SelectorNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(SelectorNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(SelectorGroupNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(SelectorGroupNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(SelectorSegmentNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(SimpleNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(SingleLineCommentNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(SpacingNode node) {
        return defaultValue();
    }

    @Override
    public boolean enter(VariableDefinitionNode node) {
        return defaultValue();
    }

    @Override
    public boolean exit(VariableDefinitionNode node) {
        return defaultValue();
    }

    @Override
    public boolean visit(VariableReferenceNode node) {
        return defaultValue();
    }
}
