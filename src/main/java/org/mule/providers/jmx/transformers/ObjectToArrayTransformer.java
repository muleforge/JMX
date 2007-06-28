package org.mule.providers.jmx.transformers;

import org.mule.umo.transformer.TransformerException;
import org.mule.transformers.AbstractTransformer;

import java.util.Collection;
import java.util.ArrayList;

public class ObjectToArrayTransformer extends AbstractTransformer {
    private int forceLength = -1;

    public int getForceLength() {
        return forceLength;
    }

    public void setForceLength(int forceLength) {
        this.forceLength = forceLength;
    }

    protected Object doTransform(Object src, String encoding) throws TransformerException {
        Object[] array;
        if (src instanceof Object[]) {
            array = (Object[]) src;
        } else if (src instanceof Collection) {
            Collection collection = (Collection) src;
            array = collection.toArray();
        } else if (src instanceof Iterable) {
            Collection<Object> collection = new ArrayList<Object>();
            for (Object o : (Iterable) src) {
                collection.add(o);
            }
            array = collection.toArray();
        } else {
            array = new Object[] { src };
        }

        if (forceLength>=0) {
            Object[] trimmed = new Object[forceLength];
            int trimLength = Math.min(forceLength, array.length);
            System.arraycopy(array, 0, trimmed, 0, trimLength);
            return trimmed;
        } else {
            return array;
        }
    }
}
