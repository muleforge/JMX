package org.mule.providers.jmx;

import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.UMOEvent;
import org.mule.umo.transformer.TransformerException;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import java.util.Properties;

class UriUtils {
    private UriUtils() { }

    public static ObjectName createObjectName(UMOEndpointURI uri) throws MalformedObjectNameException {
        String auth = uri.getAuthority();
        return auth==null ? null : new ObjectName(auth);
    }

    public static String createOperationName(UMOEndpointURI uri) {
        return uri.getResourceInfo();
    }

    public static String createAttributeName(UMOEndpointURI uri) {
        return uri.getResourceInfo();
    }

    public static Object[] createParams(UMOEndpointURI uri, UMOEvent e) throws TransformerException {
        return (Object[]) e.getTransformedMessage();
    }

    public static NotificationFilter createNotificationFilter(UMOEndpointURI endpointURI) {
        Properties params = endpointURI.getUserParams();
        if (!params.containsKey("filter")) return null;

        NotificationFilterSupport filter = new NotificationFilterSupport();
        for (String typePrefix : params.getProperty("filter").split(";")) {
            filter = new NotificationFilterSupport();
            filter.enableType(typePrefix);
        }
        return filter;
    }
}
