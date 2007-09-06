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

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.provider.MessageTypeNotSupportedException;

import javax.management.Notification;

/**
 * @author Dimitar Dimitrov
 * TODO: externalize propnames to a special JMXConstants class
 *       @link org.mule.providers.jms.JMSConstants.
 */
class NotificationAdapter extends AbstractMessageAdapter {
    private final Notification notification;

    public NotificationAdapter(Object message) throws MessageTypeNotSupportedException {
        if (!(message instanceof Notification)) {
            throw new MessageTypeNotSupportedException(message, getClass());
        }
        this.notification = (Notification) message;
        id = String.valueOf(getSequenceNumber());
        setLongProperty(JmxConnector.PROP_SEQ_NUMBER, notification.getSequenceNumber());
        setLongProperty(JmxConnector.PROP_TIMESTAMP, notification.getTimeStamp());
        setStringProperty(JmxConnector.PROP_TYPE, notification.getType());
        setStringProperty(JmxConnector.PROP_MESSAGE, notification.getMessage());
        setProperty(JmxConnector.PROP_SOURCE, notification.getSource());
        setProperty(JmxConnector.PROP_USER_DATA, notification.getUserData());
    }

    public String getPayloadAsString(String encoding) throws Exception {
        return notification.toString();
    }

    public byte[] getPayloadAsBytes() throws Exception {
        return convertToBytes(getUserData());
    }

    public Object getPayload() {
        return notification;
    }

    public String getType() {
        return notification.getType();
    }

    public long getSequenceNumber() {
        return notification.getSequenceNumber();
    }

    public long getTimeStamp() {
        return notification.getTimeStamp();
    }

    public String getMessage() {
        return notification.getMessage();
    }

    public Object getUserData() {
        return notification.getUserData();
    }

    public Object getSource() {
        return notification.getSource();
    }
}
