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

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import java.lang.reflect.Array;

public class ArraySizeNormalizer extends AbstractTransformer {
    private int desiredLength = -1;
    private boolean trimLeading;

    public boolean isTrimLeading() {
        return trimLeading;
    }

    public void setTrimLeading(boolean trimLeading) {
        this.trimLeading = trimLeading;
    }

    public int getDesiredLength() {
        return desiredLength;
    }

    public void setDesiredLength(int desiredLength) {
        this.desiredLength = desiredLength;
    }

    @SuppressWarnings({"SuspiciousSystemArraycopy"})
    protected Object doTransform(Object src, String encoding) throws TransformerException {
        int length = Array.getLength(src);
        if (desiredLength < 0 || length == desiredLength) return src;

        if (logger.isDebugEnabled()) {
            int diff = desiredLength - length;
            logger.debug(String.format(
                    "%s array to %d (%s %d elements)",
                    diff < 0 ? "Truncating" : "Padding", desiredLength,
                    diff < 0 ? "trimming" : "Padding", Math.abs(diff)
            ));
        }

        Object trimmed = Array.newInstance(src.getClass().getComponentType(), desiredLength);
        int trimLength = Math.min(desiredLength, length);
        int srcStart = Math.max(0, trimLeading ? length - trimLength : 0);
        int destStart = Math.max(0, trimLeading ? desiredLength - trimLength : 0);
        System.arraycopy(src, srcStart, trimmed, destStart, trimLength);
        return trimmed;
    }
}