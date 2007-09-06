package org.mule.providers.jmx.transformers;

import org.mule.providers.NullPayload;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.transformer.TransformerException;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes"})
public class ObjectToArrayTransformerTestCase extends AbstractMuleTestCase {
    private ObjectToArrayTransformer transformer;

    @Override
    protected void doSetUp() throws Exception {
        transformer = new ObjectToArrayTransformer();
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

        transformer.setForceLength(5);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(1, 2, 3, null, null), asList(transformed));

        transformer.setForceLength(2);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(1, 2), asList(transformed));

        transformer.setForceLength(-1);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(1, 2, 3), asList(transformed));
    }

    public void testTransformCollection() throws TransformerException {
        List<Integer> integers = asList(1, 2, 3);

        Object[] transformed = (Object[]) transformer.transform(integers);
        assertEquals(integers, asList(transformed));

        transformer.setForceLength(5);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(1, 2, 3, null, null), asList(transformed));

        transformer.setForceLength(2);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(asList(1, 2), asList(transformed));

        transformer.setForceLength(-1);
        transformed = (Object[]) transformer.transform(integers);
        assertEquals(integers, asList(transformed));
    }

    public void testTransformIterable() throws TransformerException {
        Iterable iterable = new Iterable() {
            public Iterator iterator() { return asList(1, 2, 3).iterator(); }
        };
        
        Object[] transformed = (Object[]) transformer.transform(iterable);
        assertEquals(asList(1, 2, 3), asList(transformed));

        transformer.setForceLength(5);
        transformed = (Object[]) transformer.transform(iterable);
        assertEquals(asList(1, 2, 3, null, null), asList(transformed));

        transformer.setForceLength(2);
        transformed = (Object[]) transformer.transform(iterable);
        assertEquals(asList(1, 2), asList(transformed));

        transformer.setForceLength(-1);
        transformed = (Object[]) transformer.transform(iterable);
        assertEquals(asList(1, 2, 3), asList(transformed));
    }

    public void testTransformOther() throws TransformerException {
        Object object = new Object();

        Object[] transformed = (Object[]) transformer.transform(object);
        assertEquals(asList(object), asList(transformed));

        transformer.setForceLength(5);
        transformed = (Object[]) transformer.transform(object);
        assertEquals(asList(object, null, null, null, null), asList(transformed));

        transformed = (Object[]) transformer.transform(NullPayload.getInstance());
        assertEquals(asList(null, null, null, null, null), asList(transformed));

        transformer.setForceLength(2);
        transformed = (Object[]) transformer.transform(object);
        assertEquals(asList(object, null), asList(transformed));

        transformed = (Object[]) transformer.transform(NullPayload.getInstance());
        assertEquals(asList(null, null), asList(transformed));

        transformer.setForceLength(0);
        transformed = (Object[]) transformer.transform(object);
        assertEquals(Collections.EMPTY_LIST, asList(transformed));

        transformed = (Object[]) transformer.transform(NullPayload.getInstance());
        assertEquals(Collections.EMPTY_LIST, asList(transformed));

        transformer.setForceLength(-1);
        transformed = (Object[]) transformer.transform(object);
        assertEquals(asList(object), asList(transformed));
    }
}
