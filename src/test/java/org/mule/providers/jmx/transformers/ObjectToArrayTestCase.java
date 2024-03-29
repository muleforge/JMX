/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jmx.transformers;

import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.transformer.TransformerException;

import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.List;

/** @author Dimitar Dimitrov */
@SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes"})
public class ObjectToArrayTestCase extends AbstractMuleTestCase {
    private ObjectToArray transformer;

    @Override
    protected void doSetUp() throws Exception {
        transformer = new ObjectToArray();
    }

    @Override
    protected void doTearDown() throws Exception {
        transformer = null;
    }

    public void testTransformArray() throws TransformerException {
        try {
            transformer.transform(new int[]{1, 2, 3});
            fail("Boxing of primitive arrays not implemented and not tested.");
        } catch (TransformerException e) {
        }
        Integer[] integers = {1, 2, 3};
        Object[] transformed = (Integer[]) transformer.transform(integers);
        assertEquals(asList(1, 2, 3), asList(transformed));
    }

    public void testTransformCollection() throws TransformerException {
        List<Integer> integers = asList(1, 2, 3);

        Object[] transformed = (Object[]) transformer.transform(integers);
        assertEquals(integers, asList(transformed));
    }

    public void testTransformIterable() throws TransformerException {
        Iterable iterable = new Iterable() {
            public Iterator iterator() {
                return asList(1, 2, 3).iterator();
            }
        };

        Object[] transformed = (Object[]) transformer.transform(iterable);
        assertEquals(asList(1, 2, 3), asList(transformed));
    }

    public void testTransformOther() throws TransformerException {
        Object object = new Object();

        Object[] transformed = (Object[]) transformer.transform(object);
        assertEquals(asList(object), asList(transformed));
    }
}
