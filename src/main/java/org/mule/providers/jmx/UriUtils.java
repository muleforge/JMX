package org.mule.providers.jmx;

import org.mule.umo.UMOEvent;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.transformer.TransformerException;

import javax.management.*;
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
        // TODO: shortcut for JMImplementation:type=MBeanServerDelegate.
        return uri.getResourceInfo();
    }

    public static Object[] createParams(UMOEndpointURI uri, UMOEvent e) throws TransformerException {
        return (Object[]) e.getTransformedMessage();
    }

    public static NotificationFilter createNotificationFilter(UMOEndpointURI endpointURI) {
        Properties params = endpointURI.getUserParams();
        if (params.containsKey(JmxConnector.URIPROP_FILTER_NOTIFTYPE)) {
            NotificationFilterSupport filter = new NotificationFilterSupport();
            for (String typePrefix : params.getProperty(JmxConnector.URIPROP_FILTER_NOTIFTYPE).split(";")) {
                filter.enableType(typePrefix);
            }
            return filter;
        }

        if (params.containsKey(JmxConnector.URIPROP_FILTER_ATTRIBUTES)) {
            AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
            for (String attribute : params.getProperty(JmxConnector.URIPROP_FILTER_ATTRIBUTES).split(";")) {
                filter.enableAttribute(attribute);
            }
            return filter;
        }

        // TODO: provide support for MBeanServerNotificationFilter {mbeans/types}
        return null;
    }
}
