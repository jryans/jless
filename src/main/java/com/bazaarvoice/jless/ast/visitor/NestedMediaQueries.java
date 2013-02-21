/**
 * Copyright 2013 Bazaarvoice, Inc.
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
 * @author Iaroslav Porodko (iaroslav.porodko@bazaarvoice.com)
 */
package com.bazaarvoice.jless.ast.visitor;

import com.bazaarvoice.jless.ast.node.MediaQueryNode;
import com.bazaarvoice.jless.ast.node.RuleSetNode;
import com.bazaarvoice.jless.ast.node.ScopeNode;
import com.bazaarvoice.jless.ast.node.SelectorGroupNode;
import com.bazaarvoice.jless.ast.node.SelectorNode;
import com.bazaarvoice.jless.ast.node.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.node.SpacingNode;
import com.bazaarvoice.jless.ast.node.WhiteSpaceCollectionNode;
import com.bazaarvoice.jless.ast.util.NodeTreeUtils;

import java.util.List;

public class NestedMediaQueries extends InclusiveNodeVisitor {


    /**
     * Locates nested MediaQueryNode inside RuleSetNode,
     * separates RuleSetNode and MediaQueryNode
     *
     * If MediaQueryNode has nested RuleSetNodes than to every
     * nested selectors will added selector of input RuleSetNode
     *
     * If MediaQueryNode has other nodes except WhiteSpaceCollectionNode and
     * RuleSetNode than all of that will be wrapped with new RuleSetNode with
     * the same selectors as has input RuleSetNode
     */
    @Override
    public boolean enter(RuleSetNode ruleSetNode) {

        ScopeNode scopeNode = NodeTreeUtils.getFirstChild(ruleSetNode, ScopeNode.class);
        SelectorGroupNode selectorGroupNode = NodeTreeUtils.getFirstChild(ruleSetNode, SelectorGroupNode.class);

        if (selectorGroupNode == null) {
            return true;
        }

        List<SelectorNode> selectorNodes = NodeTreeUtils.getChildren(selectorGroupNode, SelectorNode.class);

        if (selectorNodes.size() < 0) {
            return true;
        }

        List<MediaQueryNode> mediaQueryNodes = NodeTreeUtils.getAndRemoveChildren(scopeNode, MediaQueryNode.class);

        for (MediaQueryNode mediaQueryNode : mediaQueryNodes) {
            ScopeNode mediaScopeNode = NodeTreeUtils.getFirstChild(mediaQueryNode, ScopeNode.class);

            List<RuleSetNode> nestedRuleSets = NodeTreeUtils.getAndRemoveChildren(mediaScopeNode, RuleSetNode.class);

            // if scope node for media query has anything more but whitespaces and rule sets than wrap it with rule set with the same selector group as outer rule set has
            if (mediaScopeNode.getChildren().size() > NodeTreeUtils.getChildren(mediaScopeNode, WhiteSpaceCollectionNode.class).size()) {
                RuleSetNode newRuleSetNode = new RuleSetNode();
                ScopeNode newScopeNode = new ScopeNode();
                newRuleSetNode.addChild(selectorGroupNode.clone());
                newRuleSetNode.addChild(newScopeNode);

                NodeTreeUtils.moveChildren(mediaScopeNode, newScopeNode);

                mediaScopeNode.clearChildren();
                mediaScopeNode.addChild(newRuleSetNode);
            }

            // adding outer selectors to every nested selectors
            for (RuleSetNode nestedRuleSet : nestedRuleSets) {
                List<SelectorGroupNode> nestedSelectorGroupNodes = NodeTreeUtils.getChildren(nestedRuleSet, SelectorGroupNode.class);

                for (SelectorGroupNode nestedSelectorGroupNode : nestedSelectorGroupNodes) {
                    List<SelectorNode> nestedSelectorNodes = NodeTreeUtils.getAndRemoveChildren(nestedSelectorGroupNode, SelectorNode.class);
                    NodeTreeUtils.getAndRemoveChildren(nestedSelectorGroupNode, SpacingNode.class);

                    for (SelectorNode selectorNode : selectorNodes) {
                        for (SelectorNode nestedSelectorNode : nestedSelectorNodes) {
                            if (nestedSelectorNode.getChildren().get(0) != null) {
                                if (nestedSelectorNode.getChildren().get(0) instanceof SelectorSegmentNode) {
                                    SelectorSegmentNode selectorSegmentNode = (SelectorSegmentNode) nestedSelectorNode.getChildren().get(0);
                                    selectorSegmentNode.setCombinator(" ");
                                }
                            }

                            for (int j = selectorNode.getChildren().size() - 1; j >= 0; j--) {
                                if (selectorNode.getChildren().get(j) instanceof SelectorSegmentNode) {
                                    SelectorSegmentNode selectorSegmentNode = (SelectorSegmentNode) selectorNode.getChildren().get(j).clone();

                                    nestedSelectorNode.addChild(0, selectorSegmentNode);
                                }
                            }

                            nestedSelectorGroupNode.addChild(nestedSelectorNode);
                            nestedSelectorGroupNode.addChild(new SpacingNode(" "));
                        }
                    }
                }
                mediaScopeNode.addChild(nestedRuleSet);
            }

            if (ruleSetNode.getParent() != null) {
                ruleSetNode.getParent().addChild(new SpacingNode("\n"));
                ruleSetNode.getParent().addChild(mediaQueryNode);
            }
        }

        return true;
    }
}
