package org.mule.providers.jmx;

import org.mule.impl.MuleEvent;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.transformer.TransformerException;

import javax.management.*;
import java.io.IOException;

public class JmxOperationDispatcher extends AbstractMessageDispatcher {
    public JmxOperationDispatcher(UMOImmutableEndpoint endpoint) {
        super(endpoint);
    }

    protected void doConnect() throws Exception { }
    protected void doDisconnect() throws Exception { }
    protected void doDispose() { }

    protected void doDispatch(UMOEvent event) throws Exception {
        invoke(event);
    }

    protected UMOMessage doSend(UMOEvent event) throws Exception {
        return invoke(event);
    }

    protected UMOMessage doReceive(long timeout) throws Exception {
        return invoke(new MuleEvent(new MuleMessage(new Object[0]), endpoint, null, true));
    }

    private MuleMessage invoke(UMOEvent event) throws InstanceNotFoundException, IOException, ReflectionException, MBeanException, MalformedObjectNameException, IntrospectionException, ClassNotFoundException, TransformerException {
        JmxConnector c = (JmxConnector) connector;
        UMOEndpointURI uri = endpoint.getEndpointURI();
        return new MuleMessage(c.invoke(
                UriUtils.createObjectName(uri),
                UriUtils.createOperationName(uri),
                UriUtils.createParams(uri, event)
        ));
    }
}