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

import org.mule.umo.UMOEvent;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.transformer.TransformerException;

import java.util.Properties;

import javax.management.*;
import javax.management.relation.MBeanServerNotificationFilter;
import javax.management.remote.JMXConnectionNotification;
import org.apache.log4j.Logger;

class UriUtils {
    private static final Logger logger = Logger.getLogger(UriUtils.class);

    private UriUtils() {
    }

    public static ObjectName createObjectName(UMOEndpointURI uri) throws MalformedObjectNameException {
        String auth = uri.getAuthority();
        if (auth == null) throw new NullPointerException("ObjectName cannot be created from null string!");
        if (JmxEndpointBuilder.URI_AUTHORITY_CONNECTOR.equals(auth)) return null;
        if (JmxEndpointBuilder.URI_AUTHORITY_MBSDELEGATE.equals(auth))
            return new ObjectName("JMImplementation:type=MBeanServerDelegate");
        return new ObjectName(auth);
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

    public static NotificationFilter createNotificationFilter(UMOEndpointURI endpointURI) throws MalformedObjectNameException {
        Properties params = endpointURI.getUserParams();

        String auth = endpointURI.getAuthority();
        boolean isConnector = JmxEndpointBuilder.URI_AUTHORITY_CONNECTOR.equals(auth);
        boolean isDelegate = JmxEndpointBuilder.URI_AUTHORITY_MBSDELEGATE.equals(auth);

        if (isDelegate && params.containsKey(JmxEndpointBuilder.URIPROP_FILTER_NOTIFBEANS)) {
            MBeanServerNotificationFilter filter = new MBeanServerNotificationFilter();
            String beansStr = params.getProperty(JmxEndpointBuilder.URIPROP_FILTER_NOTIFBEANS);
            if (beansStr != null) {
                setNotificationMBeans(filter, beansStr.split(";"));
            }

            setNotificationTypes(filter, params, isDelegate, isConnector);
            return filter;
        }

        if (params.containsKey(JmxEndpointBuilder.URIPROP_FILTER_ATTRIBUTES)) {
            AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
            for (String attribute : params.getProperty(JmxEndpointBuilder.URIPROP_FILTER_ATTRIBUTES).split(";")) {
                filter.enableAttribute(attribute);
            }
            return filter;
        }

        if (params.containsKey(JmxEndpointBuilder.URIPROP_FILTER_NOTIFTYPE)) {
            NotificationFilterSupport filter = new NotificationFilterSupport();
            setNotificationTypes(filter, params, isDelegate, isConnector);
            return filter;
        }

        return null;
    }

    private static void setNotificationMBeans(MBeanServerNotificationFilter filter, String[] onames) throws MalformedObjectNameException {
        if (onames.length == 0) return;
        if (onames[0].equals("!*")) {
            filter.disableAllObjectNames();
        }
        for (String oname : onames) {
            boolean shallEnable = !oname.startsWith("!");
            if (shallEnable) {
                oname = oname.substring(1);
            }
            if ("*".equals(oname)) {
                if (shallEnable) {
                    filter.enableAllObjectNames();
                } else {
                    filter.disableAllObjectNames();
                }
                continue;
            }
            try {
                ObjectName objName = new ObjectName(oname);
                if (shallEnable) {
                    filter.enableObjectName(objName);
                } else {
                    filter.disableObjectName(objName);
                }
            } catch (Exception e) {
                logger.warn("Could not " + (shallEnable ? "enable" : "disable") + " " + oname, e);
            }
        }
    }

    private static void setNotificationTypes(NotificationFilterSupport filter, Properties params, boolean translateDelegateAliases, boolean translateConnectorAliases) {
        String typesListStr = params.getProperty(JmxEndpointBuilder.URIPROP_FILTER_NOTIFTYPE);
        if (typesListStr == null) return;
        String[] typePrefixes = typesListStr.split(";");
        for (String typePrefix : typePrefixes) {
            if (translateConnectorAliases) typePrefix = translateConnectorNotificationType(typePrefix);
            if (translateDelegateAliases) typePrefix = translateDelegateNotificationType(typePrefix);
            filter.enableType(typePrefix);
        }
    }

    private static String translateConnectorNotificationType(String typePrefix) {
        if (".opened".equalsIgnoreCase(typePrefix)) return JMXConnectionNotification.OPENED;
        if (".closed".equalsIgnoreCase(typePrefix)) return JMXConnectionNotification.CLOSED;
        if (".failed".equalsIgnoreCase(typePrefix)) return JMXConnectionNotification.FAILED;
        if (".notif-lost".equalsIgnoreCase(typePrefix)) return JMXConnectionNotification.NOTIFS_LOST;
        return typePrefix;
    }

    private static String translateDelegateNotificationType(String typePrefix) {
        if (".registered".equalsIgnoreCase(typePrefix)) return MBeanServerNotification.REGISTRATION_NOTIFICATION;
        if (".unregistered".equalsIgnoreCase(typePrefix)) return MBeanServerNotification.UNREGISTRATION_NOTIFICATION;
        return typePrefix;
    }
}
