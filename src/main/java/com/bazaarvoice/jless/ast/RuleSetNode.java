package com.bazaarvoice.jless.ast;

import java.util.ArrayList;
import java.util.List;

public class RuleSetNode extends Node {

    private ListNode<SelectorNode> _selectors;

    

    public void addSelector(SelectorNode selector) {
        _selectors.add(selector);
    }
}
