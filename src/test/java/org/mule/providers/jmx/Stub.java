/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jmx;

import java.util.Date;

import javax.management.NotificationBroadcasterSupport;

/**
 * @author Dimitar Dimitrov
 */
public class Stub extends NotificationBroadcasterSupport implements StubMBean {
    private Date expected;
    private volatile int version;
    private boolean updatesDisabled;

    public int getVersion() {
        return version;
    }

    public void setDisableUpdates(boolean locked) {
        this.updatesDisabled = locked;
    }

    public synchronized int awaitAsyncCall(int expectedVersion, long millis) throws InterruptedException {
        long timeout = System.currentTimeMillis() + millis;
        while (version < expectedVersion){
            wait(millis);
            if (System.currentTimeMillis() > timeout) throw new InterruptedException("Timed out without reaching the expected version: expected " + expectedVersion + "; current " + version);
        }
        return version;
    }

    public Date getExpected() {
        return expected;
    }

    public void setExpected(Date expected) {
        version++;
        if (updatesDisabled) return;
        this.expected = expected;
    }

    public synchronized void setAsyncExpected(Date expected) {
        version++;
        this.expected = expected;
        if (updatesDisabled) return;
        notifyAll();
    }

    public Date call(Date o) {
        version++;
        return o.equals(expected) ? o : null;
    }

    public Date returnExpected() {
        version++;
        return expected;
    }

    public Object selectObject(int num, Object... o) {
        version++;
        return num<0 ? null : o[num];
    }

    public synchronized void callAsync() {
        version++;
        notifyAll();
    }
}
