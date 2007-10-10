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

import org.mule.impl.NoSatisfiableMethodsException;
import org.mule.providers.AbstractConnector;
import org.mule.providers.DefaultMessageAdapter;
import org.mule.providers.NullPayload;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.umo.transformer.TransformerException;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

import javax.management.*;
import javax.management.relation.MBeanServerNotificationFilter;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

public class JmxConnector extends AbstractConnector {
    private String serviceUrl;
    private Map<String, ?> environment;
    private Subject delegationSubject;
    private MBeanServerConnection connection;
    private JMXConnector connector;
    private Map<String, ObjectName> mbeanAliases = new HashMap<String, ObjectName>();

    public JmxConnector() throws MalformedObjectNameException {
        setSupportedProtocols(Arrays.asList("jmx:attribute", "jmx:notification", "jmx:operation"));
        mbeanAliases.put(JmxEndpointBuilder.URI_AUTHORITY_CONNECTOR, null);
        mbeanAliases.put(JmxEndpointBuilder.URI_AUTHORITY_MBSDELEGATE, new ObjectName("JMImplementation:type=MBeanServerDelegate"));
    }

    public String getProtocol() {
        return "jmx";
    }

    protected void doInitialise() throws InitialisationException {
        if (serviceUrl != null) {
            try {
                JMXServiceURL url = new JMXServiceURL(serviceUrl);
                connector = JMXConnectorFactory.connect(url, environment);
            } catch (IOException e) {
                throw new InitialisationException(e, this);
            }
        }
    }

    protected void doDispose() {
        if (connector != null) {
            try {
                connector.close();
                connector = null;
            } catch (IOException e) {
                logger.error("JMX connector failed to dispose propperly: ", e);
            }
        }
    }

    protected void doStart() throws UMOException {
    }

    protected void doStop() throws UMOException {
    }

    protected void doConnect() throws Exception {
        if (connector == null) {
            connection = ManagementFactory.getPlatformMBeanServer();
        } else {
            connector.connect(environment);
            connection = connector.getMBeanServerConnection(delegationSubject);
        }
    }

    protected void doDisconnect() throws Exception {
        connection = null;
        if (connector != null) {
            connector.close();
        }
    }

    public void setMbeanAliases(Map<String, String> mbeanAliases) throws MalformedObjectNameException {
        for (Map.Entry<String, String> entry : mbeanAliases.entrySet()) {
            if (entry.getValue() == null) {
                this.mbeanAliases.remove(entry.getKey());
            } else {
                this.mbeanAliases.put(entry.getKey(), new ObjectName(entry.getValue()));
            }
        }
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public Map<String, ?> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, ?> environment) {
        this.environment = environment;
    }

    public Subject getDelegationSubject() {
        return delegationSubject;
    }

    public UMOMessageAdapter getMessageAdapter(Object message) throws MessagingException {
        if (message instanceof Notification) {
            return new NotificationAdapter(message);
        }
        return new DefaultMessageAdapter(message);
    }

    public void setDelegationSubject(Subject delegationSubject) {
        this.delegationSubject = delegationSubject;
    }


    /**
     * <p>Adds a listener to a registered MBean or the connector.</p>
     * <p/>
     * <p> A notification emitted by an MBean will be forwarded by the
     * MBeanServer to the listener.<p>
     *
     * @param endpoint The URI is mapped to the object name of the MBean
     *                 to which the listener should be added. If null and the
     *                 connector is a remote MBean server, we will start listening
     *                 listen to the connector notifications.
     * @param listener The listener object which will handle the
     *                 notifications emitted by the registered MBean.
     * @param filter   The filter object. If filter is null, no
     *                 filtering will be performed before handling notifications.
     * @param handback The context to be sent to the listener when a
     *                 notification is emitted.
     * @throws InstanceNotFoundException The MBean name provided
     *                                   does not match any of the registered MBeans.
     * @throws IOException               A communication problem occurred when
     *                                   talking to the MBean server.
     * @see MBeanServerConnection#addNotificationListener(
     *javax.management.ObjectName,
     *      javax.management.NotificationListener,
     *      javax.management.NotificationFilter, Object)
     * @see JMXConnector#addConnectionNotificationListener(
     *javax.management.NotificationListener,
     *      javax.management.NotificationFilter, Object)
     */
    public void addNotificationListener(UMOEndpoint endpoint, NotificationListener listener, Object handback) throws InstanceNotFoundException, IOException, MalformedObjectNameException {
        UMOEndpointURI uri = endpoint.getEndpointURI();
        ObjectName name = createObjectName(uri);
        NotificationFilter filter = createNotificationFilter(uri);
        if (name == null && connector != null) {
            connector.addConnectionNotificationListener(listener, filter, handback);
        } else if (name != null) {
            connection.addNotificationListener(name, listener, filter, handback);
        }
    }

    /**
     * <p>Removes a listener from a registered MBean.</p>
     * <p/>
     * <P> If the listener is registered more than once, perhaps with
     * different filters or callbacks, this method will remove all
     * those registrations.
     *
     * @param endpoint The URI is mapped to the object name of the MBean
     *                 from which the listener should be removed. If null and the
     *                 connector is a remote MBean server, we will stop listening
     *                 listen to the connector notifications.
     * @throws InstanceNotFoundException The MBean name provided
     *                                   does not match any of the registered MBeans.
     * @throws ListenerNotFoundException The listener is not
     *                                   registered in the MBean.
     * @throws IOException               A communication problem occurred when
     *                                   talking to the MBean server.
     * @see MBeanServerConnection#removeNotificationListener(
     *javax.management.ObjectName,
     *      javax.management.NotificationListener)
     * @see JMXConnector#removeConnectionNotificationListener(
     *javax.management.NotificationListener)
     */
    public void removeNotificationListener(UMOEndpoint endpoint, NotificationListener l) throws ListenerNotFoundException, InstanceNotFoundException, IOException, MalformedObjectNameException {

        UMOEndpointURI uri = endpoint.getEndpointURI();
        ObjectName name = createObjectName(uri);
        if (name == null) {
            if (connector != null) {
                connector.removeConnectionNotificationListener(l);
            }
        } else {
            connection.removeNotificationListener(name, l);
        }
    }

    public Object setAttribute(UMOImmutableEndpoint endpoint, UMOEvent event) throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException, MalformedObjectNameException, TransformerException {
        UMOEndpointURI uri = endpoint.getEndpointURI();
        String attributeName = createAttributeName(uri);
        ObjectName objectName = createObjectName(uri);

        Object[] params = createParams(uri, event);
        Object value = params == null ? null : params[0];

        Attribute attribute = new Attribute(attributeName, value);
        connection.setAttribute(objectName, attribute);
        return value;
    }


    public Object getAttribute(UMOImmutableEndpoint endpoint) throws InstanceNotFoundException, IOException, ReflectionException, AttributeNotFoundException, MBeanException, MalformedObjectNameException {
        UMOEndpointURI uri = endpoint.getEndpointURI();
        ObjectName objectName = createObjectName(uri);
        String attributeName = createAttributeName(uri);
        return connection.getAttribute(objectName, attributeName);
    }

    public Object invoke(UMOImmutableEndpoint endpoint, UMOEvent event) throws InstanceNotFoundException, IOException, ReflectionException, MBeanException, IntrospectionException, ClassNotFoundException, NoSatisfiableMethodsException, NoSatisfiableMBeanOperationsException, TooManySatisfiableMBeanOperationsException, MalformedObjectNameException, TransformerException {
        UMOEndpointURI uri = endpoint.getEndpointURI();
        ObjectName name = createObjectName(uri);
        String operationName = createOperationName(uri);
        Object[] params = createParams(uri, event);

        MBeanInfo beanInfo = connection.getMBeanInfo(name);
        List<String[]> signatures = lookupSignatures(beanInfo, operationName, params, uri.getParams().getProperty(JmxEndpointBuilder.URIPROP_SIGNATURE));

        if (signatures.isEmpty()) {
            throw new NoSatisfiableMBeanOperationsException(beanInfo, operationName, Arrays.asList(params));
        }

        if (signatures.size() > 1) {
            throw new TooManySatisfiableMBeanOperationsException(beanInfo, operationName, Arrays.asList(params), signatures);
        }

        return connection.invoke(name, operationName, params, signatures.get(0));
    }

    private List<String[]> lookupSignatures(MBeanInfo beanInfo, String operationName, Object[] params, String signatureHint) throws ClassNotFoundException {
        String[] hint = signatureHint == null ? null : signatureHint.split(";");

        List<String[]> signatures = new ArrayList<String[]>();

        next_signature:
        for (MBeanOperationInfo operationInfo : beanInfo.getOperations()) {
            if (!operationInfo.getName().equals(operationName)) continue;

            MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
            if (!checkSignatureCompatible(parameterInfos, params)) continue;

            String[] signature = new String[parameterInfos.length];
            if (hint != null && signature.length != hint.length) continue;

            for (int i = 0; i < signature.length; i++) {
                String classname = parameterInfos[i].getType();
                signature[i] = classname;
                if (hint != null && !classname.endsWith(hint[i])) {
                    continue next_signature;
                }
            }

            signatures.add(signature);
        }

        return signatures;
    }

    private boolean checkSignatureCompatible(MBeanParameterInfo[] signature, Object[] params) throws ClassNotFoundException {
        if (params == null && signature.length == 0) return true;
        if (params == null || signature.length != params.length) return false;

        for (int i = 0; i < signature.length; i++) {
            Class paramClass = params[i] == null ? null : params[i].getClass();
            if (!Class.forName(signature[i].getType()).isAssignableFrom(paramClass)) {
                return false;
            }
        }

        return true;
    }

    private ObjectName createObjectName(UMOEndpointURI uri) throws MalformedObjectNameException {
        String auth = uri.getAuthority();
        if (auth == null) throw new NullPointerException("ObjectName cannot be created from null string!");
        return mbeanAliases.containsKey(auth) ? mbeanAliases.get(auth) : new ObjectName(auth);
    }

    private String createOperationName(UMOEndpointURI uri) {
        return uri.getResourceInfo();
    }

    private String createAttributeName(UMOEndpointURI uri) {
        return uri.getResourceInfo();
    }

    Object[] createParams(UMOEndpointURI uri, UMOEvent e) throws TransformerException {
        String raw = uri.getUserParams().getProperty(JmxEndpointBuilder.URIPROP_RAW);

        Object message = raw == null || "false".equalsIgnoreCase(raw)
                ? e.getTransformedMessage()
                : e.getMessage().getPayload();

        if (message instanceof NullPayload) {
            return null;
        } else if (message instanceof Object[]) {
            return (Object[]) message;
        } else {
            return new Object[]{message};
        }
    }

    private NotificationFilter createNotificationFilter(UMOEndpointURI endpointURI) throws MalformedObjectNameException {
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
        String typesListStr = params.getProperty(JmxEndpointBuilder.URIPROP_FILTER_NOTIFTYPE);
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
