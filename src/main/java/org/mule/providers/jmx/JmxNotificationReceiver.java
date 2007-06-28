package org.mule.providers.jmx;

import org.mule.providers.AbstractMessageReceiver;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.impl.MuleMessage;

import javax.management.*;
import java.io.IOException;

public class JmxNotificationReceiver extends AbstractMessageReceiver implements NotificationListener {
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
                UriUtils.createNotificationFilter(uri)
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
            routeMessage(new MuleMessage(notification));
        } catch (UMOException e) {
            handleException(e);
        }
    }
}
