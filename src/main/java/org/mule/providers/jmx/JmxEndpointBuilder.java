package org.mule.providers.jmx;

import org.mule.impl.endpoint.AbstractEndpointBuilder;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.umo.endpoint.UMOEndpointURI;

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
