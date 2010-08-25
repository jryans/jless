package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.node.InternalNode;
import com.bazaarvoice.jless.ast.node.PlaceholderNode;
import com.bazaarvoice.jless.ast.node.SimpleNode;
import com.bazaarvoice.jless.ast.util.RandomAccessListIterator;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class NodeTest {

    public void testChildIterators() {
        InternalNode p = new PlaceholderNode();
        p.addChild(new SimpleNode("c1"));
        p.addChild(new SimpleNode("c2"));
        p.addChild(new SimpleNode("c3"));
        p.addChild(new SimpleNode("c4"));

        RandomAccessListIterator i1 = p.pushChildIterator();
        i1.next();
        RandomAccessListIterator i2 = p.pushChildIterator();
        i2.next();
        i2.next();
        RandomAccessListIterator i3 = p.pushChildIterator();
        i3.next();
        i3.next();
        i3.next();

        Assert.assertEquals(i1.nextIndex(), 1);
        Assert.assertEquals(i2.nextIndex(), 2);
        Assert.assertEquals(i3.nextIndex(), 3);
        
        p.addChild(2, new SimpleNode("c2a"));

        Assert.assertEquals(i1.nextIndex(), 1);
        Assert.assertEquals(i2.nextIndex(), 3);
        Assert.assertEquals(i3.nextIndex(), 4);
    }
}
