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

import org.mule.tck.providers.AbstractMessageAdapterTestCase;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.UMOMessageAdapter;

import java.util.ArrayList;

import javax.management.Notification;

/**
 * @author Dimitar Dimitrov
 */
public class JmxNotificationAdapterTestCase extends AbstractMessageAdapterTestCase {
    public UMOMessageAdapter createAdapter(Object payload) throws MessagingException {
        return new NotificationAdapter(payload);
    }

    public Object getValidMessage() throws Exception {
        Notification notification = new Notification("type", this, System.nanoTime(), System.currentTimeMillis(), "test notification");
        notification.setUserData(new ArrayList());
        return notification;
    }
}
