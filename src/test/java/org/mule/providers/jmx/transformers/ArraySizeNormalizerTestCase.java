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

/** @author Dimitar Dimitrov */
@SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes"})
public class ArraySizeNormalizerTestCase extends AbstractMuleTestCase {
    private ArraySizeNormalizer transformer;

    @Override
    protected void doSetUp() throws Exception {
        transformer = new ArraySizeNormalizer();
    }

    @Override
    protected void doTearDown() throws Exception {
        transformer = null;
    }

    public void testResizeTrailing() throws TransformerException {
        Integer[] integers = {1, 2, 3};

        Object[] transformed = (Integer[]) transformer.transform(integers);
        assertEquals(asList(1, 2, 3), asList(transformed));

        transformer.setDesiredLength(5);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(1, 2, 3, null, null), asList(transformed));

        transformer.setDesiredLength(2);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(1, 2), asList(transformed));

        transformer.setDesiredLength(-1);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(1, 2, 3), asList(transformed));
    }

    public void testResizeLeading() throws TransformerException {
        Integer[] integers = {1, 2, 3};
        transformer.setTrimLeading(true);

        Object[] transformed = (Integer[]) transformer.transform(integers);
        assertEquals(asList(1, 2, 3), asList(transformed));

        transformer.setDesiredLength(5);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(null, null, 1, 2, 3), asList(transformed));

        transformer.setDesiredLength(2);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(2, 3), asList(transformed));

        transformer.setDesiredLength(-1);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(1, 2, 3), asList(transformed));
    }
}