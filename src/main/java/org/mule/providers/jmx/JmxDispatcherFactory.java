package org.mule.providers.jmx;

import org.mule.providers.AbstractMessageDispatcherFactory;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.UMOException;

public class JmxDispatcherFactory extends AbstractMessageDispatcherFactory {
    public UMOMessageDispatcher create(UMOImmutableEndpoint endpoint) throws UMOException {
        String scheme = endpoint.getEndpointURI().getScheme();
        if (scheme.equals("attribute")) {
            return new JmxAttributeDispatcher(endpoint);
        } else if (scheme.equals("operation")) {
            return new JmxOperationDispatcher(endpoint);
        } else {
            throw new RuntimeException("Unsupported URI scheme: " + scheme); //TODO: add exception
        }
    }
}
