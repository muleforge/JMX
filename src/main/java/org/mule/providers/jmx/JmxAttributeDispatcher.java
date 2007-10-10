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
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;

public class JmxAttributeDispatcher extends AbstractMessageDispatcher {
    public JmxAttributeDispatcher(UMOImmutableEndpoint endpoint) {
        super(endpoint);
    }

    protected void doConnect() throws Exception {
    }

    protected void doDisconnect() throws Exception {
    }

    protected void doDispose() {
    }

    protected void doDispatch(UMOEvent event) throws Exception {
        JmxConnector c = (JmxConnector) connector;
        c.setAttribute(endpoint, event);
    }

    protected UMOMessage doReceive(long timeout) throws Exception {
        JmxConnector c = (JmxConnector) connector;
        Object response = c.getAttribute(endpoint);
        return new MuleMessage(response);
    }

    protected UMOMessage doSend(UMOEvent event) throws Exception {
        JmxConnector c = (JmxConnector) connector;
        Object valueSet = c.setAttribute(endpoint, event);
        Object valueGet = c.getAttribute(endpoint);
        return new MuleMessage(valueGet == valueSet || valueGet.equals(valueSet));
    }
}
