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
