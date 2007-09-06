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

import org.mule.impl.MuleEvent;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.transformer.TransformerException;

import java.io.IOException;

import javax.management.*;

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