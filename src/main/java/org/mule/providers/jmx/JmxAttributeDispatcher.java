package org.mule.providers.jmx;

import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.transformer.TransformerException;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.impl.MuleMessage;

import javax.management.*;
import java.io.IOException;

public class JmxAttributeDispatcher extends AbstractMessageDispatcher {
    public JmxAttributeDispatcher(UMOImmutableEndpoint endpoint) {
        super(endpoint);
    }

    protected void doConnect() throws Exception { }
    protected void doDisconnect() throws Exception { }
    protected void doDispose() { }

    protected void doDispatch(UMOEvent event) throws Exception {
        doSet(event);
    }

    protected UMOMessage doReceive(long timeout) throws Exception {
        return doGet();
    }

    protected UMOMessage doSend(UMOEvent event) throws Exception {
        Object setValue = doSet(event);
        UMOMessage getValue = doReceive(-1);
        return new MuleMessage(getValue.getPayload().equals(setValue));
    }

    private Object doSet(UMOEvent event) throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException, MalformedObjectNameException, TransformerException {
        JmxConnector c = (JmxConnector) connector;
        UMOEndpointURI uri = endpoint.getEndpointURI();
        Object value = event.getTransformedMessage();
        c.setAttribute(
                UriUtils.createObjectName(uri),
                new Attribute(UriUtils.createAttributeName(uri), value)
        );
        return value;
    }

    private UMOMessage doGet() throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException, MalformedObjectNameException {
        JmxConnector c = (JmxConnector) connector;
        UMOEndpointURI uri = endpoint.getEndpointURI();
        return new MuleMessage(c.getAttribute(
                UriUtils.createObjectName(uri),
                UriUtils.createAttributeName(uri)
        ));
    }
}