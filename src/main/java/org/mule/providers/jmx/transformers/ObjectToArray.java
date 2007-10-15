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

import org.mule.providers.NullPayload;
import org.mule.providers.jmx.JmxMessages;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import java.util.ArrayList;
import java.util.Collection;

public class ObjectToArray extends AbstractTransformer {
    protected Object doTransform(Object src, String encoding) throws TransformerException {
        if (src instanceof NullPayload) return new Object[]{null};

        Class componentType = src.getClass().getComponentType();
        if (componentType != null) {
            if (componentType.isPrimitive()) {
                // TODO: support boxing of primitive arrays
                throw new TransformerException(JmxMessages.primitiveArraysNotSupported(), this);
            }
            return src;
        }

        if (src instanceof Collection) {
            Collection collection = (Collection) src;
            return collection.toArray();
        }
        if (src instanceof Iterable) {
            Collection<Object> collection = new ArrayList<Object>();
            for (Object o : (Iterable) src) {
                collection.add(o);
            }
            return collection.toArray();
        }

        return new Object[]{src};
    }
}
