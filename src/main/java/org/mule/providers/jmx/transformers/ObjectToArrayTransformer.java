package org.mule.providers.jmx.transformers;

import org.mule.config.i18n.MessageFactory;
import org.mule.providers.NullPayload;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

public class ObjectToArrayTransformer extends AbstractTransformer {
    private int forceLength = -1;

    public int getForceLength() {
        return forceLength;
    }

    public void setForceLength(int forceLength) {
        this.forceLength = forceLength;
    }

    protected Object doTransform(Object src, String encoding) throws TransformerException {
        if (src instanceof NullPayload) src = new Object[] { null };
        Object[] array;

        Class sclass = src.getClass();

        if (sclass.isArray() && sclass.getComponentType().isPrimitive()) {
            // TODO: support boxing of primitive arrays
            throw new TransformerException(
                    MessageFactory.createStaticMessage("Boxing of prmitive arrays is not supported yet."),
                    this
            );
        } else if (sclass.isArray()) {
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

        if (forceLength>=0 && array.length!=forceLength) {
            if (logger.isDebugEnabled()) {
                int diff = forceLength - array.length;
                logger.debug(String.format(
                        "%s array to %d (%s %d elements)",
                        diff<0 ? "Truncating" : "Padding", forceLength,
                        diff<0 ? "trimming" : "Padding", Math.abs(diff)
                ));
            }

            Object[] trimmed = (Object[]) Array.newInstance(array.getClass().getComponentType(), forceLength);
            int trimLength = Math.min(forceLength, array.length);
            System.arraycopy(array, 0, trimmed, 0, trimLength);
            return trimmed;
        } else {
            return array;
        }
    }
}
