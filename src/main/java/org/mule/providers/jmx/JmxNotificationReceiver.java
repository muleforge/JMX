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

import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;

import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;

public class JmxNotificationReceiver extends AbstractMessageReceiver implements NotificationListener {
    private Object handback;

    public JmxNotificationReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
        super(connector, component, endpoint);
    }

    protected void doStart() throws UMOException {
    }

    protected void doStop() throws UMOException {
    }

    protected void doConnect() throws Exception {
        JmxConnector c = (JmxConnector) connector;
        UMOEndpointURI uri = endpoint.getEndpointURI();
        c.addNotificationListener(
                UriUtils.createObjectName(uri),
                this,
                UriUtils.createNotificationFilter(uri),
                handback
        );
    }

    protected void doDisconnect() throws Exception {
        JmxConnector c = (JmxConnector) connector;
        UMOEndpointURI uri = endpoint.getEndpointURI();
        c.removeNotificationListener(
                UriUtils.createObjectName(uri),
                this
        );
    }

    protected void doDispose() {
    }

    public void handleNotification(Notification notification, Object handback) {
        try {
            Map<String, Object> props = new HashMap<String, Object>();
            if (handback!=null) {
                props.put(JmxConstants.PROP_HANDBACK, handback);
            }
            routeMessage(new MuleMessage(notification, props));
        } catch (UMOException e) {
            handleException(e);
        }
    }

    public Object getHandback() {
        return handback;
    }

    public void setHandback(Object handback) {
        this.handback = handback;
    }
}
