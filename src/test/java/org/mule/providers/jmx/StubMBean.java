package org.mule.providers.jmx;

import java.util.Date;

/**
 * @author Dimitar Dimitrov
 */
public interface StubMBean {
    Date call(Date str);

    void callAsync();

    Object selectObject(int num, Object... o);

    Date returnExpected();

    Date getExpected();

    void setExpected(Date expected);

    void setAsyncExpected(Date expected);
}
