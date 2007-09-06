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
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.NullPayload;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.transformer.TransformerException;

import java.io.IOException;

import javax.management.*;

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
        Object nullablePayload = value instanceof NullPayload ? null : value;
        Attribute attribute = new Attribute(UriUtils.createAttributeName(uri), nullablePayload);
        c.setAttribute(UriUtils.createObjectName(uri), attribute);
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
