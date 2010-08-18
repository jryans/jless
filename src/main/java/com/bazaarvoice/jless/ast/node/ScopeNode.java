package com.bazaarvoice.jless.ast.node;

import com.bazaarvoice.jless.ast.util.MutableTreeUtils;
import com.bazaarvoice.jless.ast.visitor.InclusiveNodeVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeAdditionVisitor;
import com.bazaarvoice.jless.ast.visitor.NodeTraversalVisitor;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

public class ScopeNode extends InternalNode {

    private final Map<String, ExpressionGroupNode> _variableNameToValueMap = new HashMap<String, ExpressionGroupNode>();
    private final Map<String, RuleSetNode> _selectorGroupToRuleSetMap = new HashMap<String, RuleSetNode>();

    public ScopeNode() {
        super();

        // Some nodes are captured in additional structures to aid later resolution
        _additionVisitor = new InclusiveNodeVisitor() {
            /**
             * Store the rule set's scope by selector group
             */
            @Override
            public boolean add(RuleSetNode node) {
                SelectorGroupNode selectorGroup = MutableTreeUtils.getFirstChild(node, SelectorGroupNode.class);
                _selectorGroupToRuleSetMap.put(selectorGroup.toString(), node);
                return super.add(node);
            }

            /**
             * Absorb all children of the given scope. This assumes that cloning is not necessary.
             */
            @Override
            public boolean add(ScopeNode node) {
                MutableTreeUtils.moveChildren(node, ScopeNode.this);
                return false; // Don't add the original scope itself
            }

            /**
             * Store variable definitions in a map by name
             */
            @Override
            public boolean add(VariableDefinitionNode node) {
                // "Variables" lock on first definition
                String name = node.getName();
                if (!_variableNameToValueMap.containsKey(name)) {
                    _variableNameToValueMap.put(name, MutableTreeUtils.getFirstChild(node, ExpressionGroupNode.class));
                }
                return super.add(node);
            }
        };
    }

    public ExpressionGroupNode getVariable(String name) {
        return _variableNameToValueMap.get(name);
    }

    public RuleSetNode getRuleSet(String selectorGroup) {
        return _selectorGroupToRuleSetMap.get(selectorGroup);
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
}
