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

import org.mule.impl.endpoint.AbstractEndpointBuilder;
import org.mule.umo.endpoint.MalformedEndpointException;

import java.net.URI;
import java.util.Properties;

/**
 * @author Dimitar Dimitrov
 */
public class JmxEndpointBuilder extends AbstractEndpointBuilder {
    protected void setEndpoint(URI uri, Properties props) throws MalformedEndpointException {
        String path = uri.getPath();
        String authority = uri.getAuthority();
        address = uri.getScheme() + "://" + (authority==null ? "" : authority) + (path==null ? "" : path);
        if (path!=null && path.startsWith("/")) {
            props.put("resourceInfo",  path.substring(1));
        }
    }
}
