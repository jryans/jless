package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.util.NodeTreeUtils;
import com.bazaarvoice.jless.ast.visitor.InclusiveNodeVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;
import com.bazaarvoice.jless.exception.IllegalMixinArgumentException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class ScopeNode extends InternalNode {

    private Map<String, ExpressionGroupNode> _variableNameToValueMap = new HashMap<String, ExpressionGroupNode>();
    private Map<String, RuleSetNode> _selectorToRuleSetMap = new HashMap<String, RuleSetNode>();
    private List<String> _parameterNames = new ArrayList<String>();
    private ScopeNode _parentScope;

    public ScopeNode() {
        super();
        setAdditionVisitor();
    }

    public ScopeNode(Node node) {
        this();
        addChild(node);
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

    /**
     * Creates a clone of this scope to be attached to the tree at the site of a mixin reference. If an ArgumentsNode is passed,
     * each of its values override those defined by the mixin's parameters.
     */
    public ScopeNode callMixin(String name, ArgumentsNode arguments) {
        List<ExpressionGroupNode> argumentList = (arguments != null) ? NodeTreeUtils.getChildren(arguments, ExpressionGroupNode.class) : Collections.<ExpressionGroupNode>emptyList();
        if (argumentList.size() > _parameterNames.size()) {
            throw new IllegalMixinArgumentException(name, _parameterNames.size());
        }

        ScopeNode mixinScope = clone();

        // Filter out any line breaks
        mixinScope.filter(new InclusiveNodeVisitor() {
            @Override
            public boolean visit(LineBreakNode node) {
                return false;
            }
        });

        // If arguments were passed, apply them
        for (int i = 0; i < argumentList.size(); i++) {
            ExpressionGroupNode argument = argumentList.get(i);
            mixinScope._variableNameToValueMap.put(_parameterNames.get(i), argument);
        }

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
                    _parameterNames.add(variable.getName());
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
                    // TODO: Optimize
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
        });
    }

    @Override
    public boolean add(NodeAdditionVisitor visitor) {
        return visitor.add(this);
    }

    @Override
    public boolean filter(NodeTraversalVisitor visitor) {
        if (visitor.enter(this)) {
            ListIterator<Node> it = pushChildIterator();
            while (it.hasNext()) {
                Node child = it.next();
                if (!child.filter(visitor)) {
                    it.remove();
                }
            }
            popChildIterator();
        }

        return visitor.visit(this);
    }

    @Override
    public boolean traverse(NodeTraversalVisitor visitor) {
        if (!isVisible()) {
            return true;
        }

        if (visitor.enter(this)) {
            ListIterator<Node> it = pushChildIterator();
            while (it.hasNext()) {
                Node child = it.next();
                if (!child.traverse(visitor)) {
                    break;
                }
            }
            popChildIterator();
        }

        return visitor.visit(this);
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
        scope._parameterNames = new ArrayList<String>();
        scope.setAdditionVisitor();

        super.cloneChildren(node);
    }
}
