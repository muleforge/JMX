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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.management.Notification;

/**
 * @author Dimitar Dimitrov
 */
public class NotificationReceiverStub {
    private final BlockingQueue<Notification> notificationPostbox = new LinkedBlockingQueue<Notification>();

    public void receive(Notification n) {
        notificationPostbox.add(n);
    }

    public Notification retreive(int timeout) throws InterruptedException {
        long start = System.currentTimeMillis();
        Notification notification = notificationPostbox.poll((long) (timeout * 1.5), TimeUnit.MILLISECONDS);
        if (start+ timeout < System.currentTimeMillis()) {
            throw new InterruptedException("Timed out: " + timeout + "ms.");
        }
        return notification;
    }
}
