package org.mule.providers.jmx;

/**
 * @author Dimitar Dimitrov
 */
public class Stub implements StubMBean {
    private Object expected;
    private int called;

    public Stub(Object expected) {
        this.expected = expected;
    }

    public int getCalled() {
        return called;
    }

    public Object call(Object o) {
        called++;
        return o.equals(expected) ? o : null;
    }
}
