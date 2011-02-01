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
import com.bazaarvoice.jless.exception.IllegalMixinArgumentException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScopeNode extends InternalNode {

    private static final String FILTER_PROPERTY = "filter";

    private Map<String, ExpressionGroupNode> _variableNameToValueMap = new HashMap<String, ExpressionGroupNode>();
    private Map<String, RuleSetNode> _selectorToRuleSetMap = new HashMap<String, RuleSetNode>();
    private List<VariableDefinitionNode> _parameterDefinitions = new ArrayList<VariableDefinitionNode>();
    private Map<String, PropertyNode> _propertyNameToNodeMap = new HashMap<String, PropertyNode>();
    private ScopeNode _parentScope;
    private boolean _bracketsDisplayed = true;

    public ScopeNode() {
        super();
        setAdditionVisitor();
    }

    public ScopeNode(Node node) {
        this();
        addChild(node);
    }

    public ScopeNode(Collection<Node> nodeCollection) {
        this();
        addChildren(nodeCollection);
    }

    public boolean isVariableDefined(String name) {
        return _variableNameToValueMap.containsKey(name);
    }

    public ExpressionGroupNode getVariable(String name) {
        return _variableNameToValueMap.get(name);
    }

    public RuleSetNode getRuleSet(String selectorGroup) {
        return _selectorToRuleSetMap.get(selectorGroup);
    }

    public ScopeNode getParentScope() {
        return _parentScope;
    }

    public void setParentScope(ScopeNode parentScope) {
        _parentScope = parentScope;
    }

    public boolean isBracketsDisplayed() {
        return _bracketsDisplayed;
    }

    public void setBracketsDisplayed(boolean bracketsDisplayed) {
        _bracketsDisplayed = bracketsDisplayed;
    }

    /**
     * Creates a clone of this scope to be attached to the tree at the site of a mixin reference. If an ArgumentsNode is passed,
     * each of its values override those defined by the mixin's parameters.
     */
    public ScopeNode callMixin(String name, ArgumentsNode arguments) {
        List<ExpressionGroupNode> argumentList = (arguments != null) ? NodeTreeUtils.getChildren(arguments, ExpressionGroupNode.class) : Collections.<ExpressionGroupNode>emptyList();
        if (argumentList.size() > _parameterDefinitions.size()) {
            throw new IllegalMixinArgumentException(name, _parameterDefinitions.size());
        }

        // Clone scope and filter out any white space
        ScopeNode mixinScope = clone();
        NodeTreeUtils.filterLineBreaks(mixinScope);

        // If arguments were passed, apply them
        for (int i = 0; i < argumentList.size(); i++) {
            ExpressionGroupNode argument = argumentList.get(i);
            // Replace the value of the definition
            VariableDefinitionNode parameter = mixinScope._parameterDefinitions.get(i);
            parameter.clearChildren();
            parameter.addChild(argument);
        }

        // Mark this scope's containing rule set as invisible since it has been used as a mixin
        getParent().setVisible(false);

        return mixinScope;
    }

    /**
     * Some nodes are captured in additional structures to aid later resolution.
     */
    private void setAdditionVisitor() {
        setAdditionVisitor(new InclusiveNodeVisitor() {
            /**
             * Add parameter set as a child for printing input, but also add each defined value to the variable map.
             */
            @Override
            public boolean add(ParametersNode node) {
                for (VariableDefinitionNode variable : NodeTreeUtils.getChildren(node, VariableDefinitionNode.class)) {
                    _parameterDefinitions.add(variable);
                    add(variable);
                }
                return super.add(node);
            }

            /**
             * Store the rule set's scope by selector group
             */
            @Override
            public boolean add(RuleSetNode node) {
                SelectorGroupNode selectorGroup = NodeTreeUtils.getFirstChild(node, SelectorGroupNode.class);
                for (SelectorNode selectorNode : NodeTreeUtils.getChildren(selectorGroup, SelectorNode.class)) {
                    StringBuilder sb = new StringBuilder();
                    for (Node selectorChild : selectorNode.getChildren()) {
                        sb.append(selectorChild.toString());
                    }
                    String selector = sb.toString();
                    // Mixins lock on first definition
                    if (!_selectorToRuleSetMap.containsKey(selector)) {
                        _selectorToRuleSetMap.put(selector, node);
                    }
                }
                return super.add(node);
            }

            /**
             * Absorb all children of the given scope. This assumes that cloning is not necessary.
             */
            @Override
            public boolean add(ScopeNode node) {
                NodeTreeUtils.moveChildren(node, ScopeNode.this);
                return false; // Don't add the original scope itself
            }

            /**
             * Store variable definitions in a map by name
             */
            @Override
            public boolean add(VariableDefinitionNode node) {
                String name = node.getName();
                // "Variables" lock on first definition
                if (!_variableNameToValueMap.containsKey(name)) {
                    _variableNameToValueMap.put(name, NodeTreeUtils.getFirstChild(node, ExpressionGroupNode.class));
                }
                return super.add(node);
            }

            /**
             * Store property nodes by name. If there are multiple properties for a given name, only retain the last one.
             */
            @Override
            public boolean add(PropertyNode node) {
                String name = node.getName();

                // If this is the IE-specific "filter" property, always add it
                if (name.equals(FILTER_PROPERTY)) {
                    return super.add(node);
                }

                // If the value of this property node is a vendor-specific keyword, always add it
                if (node.getChildren().get(0).toString().startsWith("-")) {
                    return super.add(node);
                }

                // Check if this property has been seen before
                if (_propertyNameToNodeMap.containsKey(name)) {
                    PropertyNode oldPropertyNode = _propertyNameToNodeMap.get(name);
                    int oldPropertyIndex = getChildren().indexOf(oldPropertyNode);

                    if (oldPropertyNode.isVisible()) {
                        // Hide the unneeded property
                        oldPropertyNode.setVisible(false);

                        // Attempt to hide one surrounding white space node
                        if (!hideWhiteSpaceNode(oldPropertyIndex - 1)) {
                            hideWhiteSpaceNode(oldPropertyIndex + 1);
                        }
                    }
                }

                // Store the property as the latest for this name
                _propertyNameToNodeMap.put(name, node);

                return super.add(node);
            }
        });
    }

    private boolean hideWhiteSpaceNode(int index) {
        if (index < 0 || index >= getChildren().size()) {
            return false;
        }

        Node node = getChildren().get(index);
        if (!(node instanceof WhiteSpaceCollectionNode || node instanceof SpacingNode) || !node.isVisible()) {
            return false;
        }

        node.setVisible(false);
        return true;
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
    public ScopeNode clone() {
        return (ScopeNode) super.clone();
    }

    /**
     * Recreate internal state before children are cloned.
     */
    @Override
    protected void cloneChildren(InternalNode node) {
        ScopeNode scope = (ScopeNode) node;

        // Reset internal state
        scope._variableNameToValueMap = new HashMap<String, ExpressionGroupNode>();
        scope._selectorToRuleSetMap = new HashMap<String, RuleSetNode>();
        scope._parameterDefinitions = new ArrayList<VariableDefinitionNode>();
        scope._propertyNameToNodeMap = new HashMap<String, PropertyNode>();
        scope.setAdditionVisitor();

        super.cloneChildren(node);
    }
}
