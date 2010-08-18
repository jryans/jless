package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.node.InternalNode;
import com.bazaarvoice.jless.ast.node.Node;
import com.bazaarvoice.jless.ast.node.PlaceholderNode;
import com.bazaarvoice.jless.ast.node.SelectorSegmentNode;
import com.bazaarvoice.jless.ast.node.SimpleNode;
import com.bazaarvoice.jless.ast.util.RandomAccessListIterator;
import com.bazaarvoice.jless.ast.visitor.Printer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 */
@Test
public class NodeTest {

    public void testChildIterators() {
        InternalNode p = new PlaceholderNode();
        p.addChild(new SimpleNode("c1"));
        p.addChild(new SimpleNode("c2"));
        p.addChild(new SimpleNode("c3"));
        p.addChild(new SimpleNode("c4"));

        RandomAccessListIterator i1 = p.pushChildIterator(1);
        RandomAccessListIterator i2 = p.pushChildIterator(2);
        RandomAccessListIterator i3 = p.pushChildIterator(3);

        Assert.assertEquals(i1.nextIndex(), 1);
        Assert.assertEquals(i2.nextIndex(), 2);
        Assert.assertEquals(i3.nextIndex(), 3);
        
        p.addChild(2, new SimpleNode("c2a"));

        Assert.assertEquals(i1.nextIndex(), 1);
        Assert.assertEquals(i2.nextIndex(), 3);
        Assert.assertEquals(i3.nextIndex(), 4);
    }

    public void testReflect() {
        long start;

        Printer p = new Printer();

        Node n = new SelectorSegmentNode("test", "elem");

        start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            n.accept(p);
        }
        TestUtils.getLog().println("Regular: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            n.acceptReflect(p);
        }
        TestUtils.getLog().println("Reflect: " + (System.currentTimeMillis() - start));

        TestUtils.flushLog();
    }
}
