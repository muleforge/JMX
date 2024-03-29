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
import static org.mule.providers.jmx.JmxMessages.noSatisfiableOperations;
import static org.mule.providers.jmx.JmxMessages.tooManySatisfiableOperations;
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

    public UMOMessageAdapter getMessageAdapter(Object message) throws MessagingException {
        if (message instanceof Notification) {
            return new NotificationAdapter(message);
        }
        return new DefaultMessageAdapter(message);
    }

    /**
     * <p>The MBean aliases are used when resolving the endpoint URI.
     * If the URI authority matches an alias, the registered ObjectName is used.
     * Otherwise, we assume that the authority is a strong representation of ObjectName.</p>
     * <p>This method deviates from the regular setter contract, because it appends
     * the content of the argument to the existing state. This makes more sense in the
     * context of configuration as wwe usually want to add our mappings to the defaults
     * and not to replace them. If particular key maps to null, the mapping is removed.<p>
     *
     * @param mbeanAliases a map from string identifiers to string representations
     *                     of ObjectNames or nulls.
     * @throws MalformedObjectNameException if a value is not a valid object name or null.
     */
    public void setMbeanAliases(Map<String, String> mbeanAliases) throws MalformedObjectNameException {
        for (Map.Entry<String, String> entry : mbeanAliases.entrySet()) {
            if (entry.getValue() == null) {
                this.mbeanAliases.remove(entry.getKey());
            } else {
                this.mbeanAliases.put(entry.getKey(), new ObjectName(entry.getValue()));
            }
        }
    }

    /** The JMX service URL if it is a remote connector. Null if it is local. */
    public String getServiceUrl() {
        return serviceUrl;
    }

    /**
     * Defines the MBean server to connect to.
     *
     * @param serviceUrl - JMX service URL for remote connection. Null if it is local.
     */
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * Gets the environment used for establishing JMX connection.
     *
     * @return String to Object map passed to the connector.connect()
     */
    public Map<String, ?> getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment used for establishing JMX connection.
     *
     * @param environment String to Object map passed to the connector.connect()
     */
    public void setEnvironment(Map<String, ?> environment) {
        this.environment = environment;
    }

    /**
     * Sets the JAAS delegation subject on behalf of which we log in.
     *
     * @return the JAAS delegation subject.
     */
    public Subject getDelegationSubject() {
        return delegationSubject;
    }

    /**
     * Gets the JAAS delegation subject on behalf of which we log in.
     *
     * @param delegationSubject the JAAS delegation subject
     */
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
        ObjectName name = resolveObjectName(uri);
        NotificationFilter filter = (NotificationFilter) endpoint.getProperty(JmxEndpointBuilder.PROP_FILTER);
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
    public void removeNotificationListener(UMOEndpoint endpoint, NotificationListener listener, Object handback) throws ListenerNotFoundException, InstanceNotFoundException, IOException, MalformedObjectNameException {
        UMOEndpointURI uri = endpoint.getEndpointURI();
        ObjectName name = resolveObjectName(uri);
        NotificationFilter filter = (NotificationFilter) endpoint.getProperty(JmxEndpointBuilder.PROP_FILTER);
        if (name == null) {
            if (connector != null) {
                connector.removeConnectionNotificationListener(listener);
            } else {
                logger.warn(JmxMessages.ignoringRemoveListenerOnPlatformMBS());
            }
        } else {
            connection.removeNotificationListener(name, listener, filter, handback);
        }
    }

    /**
     * Sets the attribute denoted by the endpoint to the payload.
     *
     * @param endpoint an attribute-type endpoint
     * @param event    wrapping the value to which we want to set the attribute.
     * @return the actual extracted value which was used.
     */
    public Object setAttribute(UMOImmutableEndpoint endpoint, UMOEvent event) throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException, MalformedObjectNameException, TransformerException {
        UMOEndpointURI uri = endpoint.getEndpointURI();
        String attributeName = uri.getResourceInfo();
        ObjectName objectName = resolveObjectName(uri);

        Object[] params = wrapArguments(uri, event);
        Object value = params == null ? null : params[0];

        Attribute attribute = new Attribute(attributeName, value);
        connection.setAttribute(objectName, attribute);
        return value;
    }

    /**
     * Gets the value of an attribute denoted by the endpoint.
     *
     * @param endpoint an attribute-type endpoint
     * @return the attribute value.
     */
    public Object getAttribute(UMOImmutableEndpoint endpoint) throws InstanceNotFoundException, IOException, ReflectionException, AttributeNotFoundException, MBeanException, MalformedObjectNameException {
        UMOEndpointURI uri = endpoint.getEndpointURI();
        ObjectName objectName = resolveObjectName(uri);
        String attributeName = uri.getResourceInfo();
        return connection.getAttribute(objectName, attributeName);
    }

    /**
     * Invokes the operation denoted by the endpoint, passing the payload as argument.
     *
     * @param endpoint an operation-type endpoint
     * @param event    wrapping the value(s) to which we want to pass as argument.
     * @return the result of the operation.
     */
    public Object invoke(UMOImmutableEndpoint endpoint, UMOEvent event) throws InstanceNotFoundException, IOException, ReflectionException, MBeanException, IntrospectionException, ClassNotFoundException, NoSatisfiableMethodsException, JmxEndpointResolutionException, MalformedObjectNameException, TransformerException {
        UMOEndpointURI uri = endpoint.getEndpointURI();
        ObjectName name = resolveObjectName(uri);
        String operationName = uri.getResourceInfo();
        Object[] params = wrapArguments(uri, event);

        MBeanInfo beanInfo = connection.getMBeanInfo(name);
        List<String[]> signatures = lookupSignatures(beanInfo, operationName, params, uri.getParams().getProperty(JmxEndpointBuilder.URIPROP_SIGNATURE));

        if (signatures.isEmpty()) {
            throw new JmxEndpointResolutionException(noSatisfiableOperations(beanInfo, operationName, Arrays.asList(params)));
        }

        if (signatures.size() > 1) {
            throw new JmxEndpointResolutionException(tooManySatisfiableOperations(beanInfo, operationName, Arrays.asList(params), signatures));
        }

        return connection.invoke(name, operationName, params, signatures.get(0));
    }

    private ObjectName resolveObjectName(UMOEndpointURI uri) throws MalformedObjectNameException {
        String auth = uri.getAuthority();
        if (auth == null) throw new NullPointerException("ObjectName cannot be created from null string!");
        return mbeanAliases.containsKey(auth) ? mbeanAliases.get(auth) : new ObjectName(auth);
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

    /*
     * TODO: replace with transformer
     */
    private Object[] wrapArguments(UMOEndpointURI uri, UMOEvent e) throws TransformerException {
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

}
