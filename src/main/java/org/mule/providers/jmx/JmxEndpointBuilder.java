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

import javax.management.*;
import javax.management.relation.MBeanServerNotificationFilter;
import javax.management.remote.JMXConnectionNotification;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** @author Dimitar Dimitrov */
public class JmxEndpointBuilder extends AbstractEndpointBuilder {

    /** logger used by this class */
    protected final Log logger = LogFactory.getLog(getClass());

    public static final String URIPROP_FILTER_NOTIFBEANS = "beans";
    public static final String URIPROP_FILTER_NOTIFTYPE = "types";
    public static final String URIPROP_FILTER_ATTRIBUTES = "attributes";
    public static final String URIPROP_SIGNATURE = "signature";
    public static final String URIPROP_RAW = "raw";
    public static final String URI_AUTHORITY_CONNECTOR = "~connector";
    public static final String URI_AUTHORITY_MBSDELEGATE = "~delegate";
    public static final String PROP_FILTER = "jmx.filter";

    protected void setEndpoint(URI uri, Properties props) throws MalformedEndpointException {
        String path = uri.getPath();
        String authority = uri.getAuthority();
        address = uri.getScheme() + "://" + (authority == null ? "" : authority) + (path == null ? "" : path);
        if (path != null && path.startsWith("/")) {
            props.put("resourceInfo", path.substring(1));
        }

        if (uri.getScheme().equals("notification")) {
            try {
                NotificationFilter filter = createNotificationFilter(authority, props);
                if (filter != null) {
                    props.put(PROP_FILTER, filter);
                }
            } catch (MalformedObjectNameException e) {
                throw new MalformedEndpointException(address, e);
            }
        }
    }

    private NotificationFilter createNotificationFilter(String authority, Properties params) throws MalformedObjectNameException {

        boolean isConnector = URI_AUTHORITY_CONNECTOR.equals(authority);
        boolean isDelegate = URI_AUTHORITY_MBSDELEGATE.equals(authority);

        if (isDelegate && params.containsKey(URIPROP_FILTER_NOTIFBEANS)) {
            MBeanServerNotificationFilter filter = new MBeanServerNotificationFilter();
            String beansStr = params.getProperty(URIPROP_FILTER_NOTIFBEANS);
            if (beansStr != null) {
                setNotificationMBeans(filter, beansStr.split(";"));
            }

            setNotificationTypes(filter, params, isDelegate, isConnector);
            return filter;
        }

        if (params.containsKey(URIPROP_FILTER_ATTRIBUTES)) {
            AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
            for (String attribute : params.getProperty(URIPROP_FILTER_ATTRIBUTES).split(";")) {
                filter.enableAttribute(attribute);
            }
            return filter;
        }

        if (params.containsKey(URIPROP_FILTER_NOTIFTYPE)) {
            NotificationFilterSupport filter = new NotificationFilterSupport();
            setNotificationTypes(filter, params, isDelegate, isConnector);
            return filter;
        }

        return null;
    }

    private void setNotificationMBeans(MBeanServerNotificationFilter filter, String[] onames) throws MalformedObjectNameException {
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

    private void setNotificationTypes(NotificationFilterSupport filter, Properties params, boolean translateDelegateAliases, boolean translateConnectorAliases) {
        String typesListStr = params.getProperty(URIPROP_FILTER_NOTIFTYPE);
        if (typesListStr == null) return;
        String[] typePrefixes = typesListStr.split(";");
        for (String typePrefix : typePrefixes) {
            if (translateConnectorAliases) typePrefix = translateConnectorNotificationType(typePrefix);
            if (translateDelegateAliases) typePrefix = translateDelegateNotificationType(typePrefix);
            filter.enableType(typePrefix);
        }
    }

    private String translateConnectorNotificationType(String typePrefix) {
        if (".opened".equalsIgnoreCase(typePrefix)) return JMXConnectionNotification.OPENED;
        if (".closed".equalsIgnoreCase(typePrefix)) return JMXConnectionNotification.CLOSED;
        if (".failed".equalsIgnoreCase(typePrefix)) return JMXConnectionNotification.FAILED;
        if (".notif-lost".equalsIgnoreCase(typePrefix)) return JMXConnectionNotification.NOTIFS_LOST;
        return typePrefix;
    }

    private String translateDelegateNotificationType(String typePrefix) {
        if (".registered".equalsIgnoreCase(typePrefix)) return MBeanServerNotification.REGISTRATION_NOTIFICATION;
        if (".unregistered".equalsIgnoreCase(typePrefix)) return MBeanServerNotification.UNREGISTRATION_NOTIFICATION;
        return typePrefix;
    }
}
