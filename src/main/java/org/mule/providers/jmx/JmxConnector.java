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
import org.mule.umo.MessagingException;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageAdapter;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

public class JmxConnector extends AbstractConnector {
    private String serviceUrl;
    private Map<String,?> environment;
    private Subject delegationSubject;
    private MBeanServerConnection connection;
    private JMXConnector connector;

    public JmxConnector() {
        setSupportedProtocols(Arrays.asList("jmx:attribute", "jmx:notification", "jmx:operation"));
    }

    public String getProtocol() {
        return "jmx";
    }

    protected void doInitialise() throws InitialisationException {
        if (serviceUrl!=null) {
            try {
                JMXServiceURL url = new JMXServiceURL(serviceUrl);
                connector = JMXConnectorFactory.connect(url, environment);
            } catch (IOException e) {
                throw new InitialisationException(e, this);
            }
        }
    }

    protected void doDispose() {
        if (connector!=null) {
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
        if (connector==null) {
            connection = ManagementFactory.getPlatformMBeanServer();
        } else {
            connector.connect(environment);
            connection = connector.getMBeanServerConnection(delegationSubject);
        }
    }

    protected void doDisconnect() throws Exception {
        connection = null;
        if (connector!=null) {
            connector.close();
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
     *
     * <p> A notification emitted by an MBean will be forwarded by the
     * MBeanServer to the listener.<p>
     *
     * @param name The name of the MBean on which the listener should
     *        be added. If null and the connector is a remote MBean
     *        server, we will listen to the connector notifications.
     * @param listener The listener object which will handle the
     *        notifications emitted by the registered MBean.
     * @param filter The filter object. If filter is null, no
     *        filtering will be performed before handling notifications.
     * @param handback The context to be sent to the listener when a
     *         notification is emitted.
     *
     * @exception InstanceNotFoundException The MBean name provided
     *            does not match any of the registered MBeans.
     * @exception IOException A communication problem occurred when
     *            talking to the MBean server.
     *
     * @see MBeanServerConnection#addNotificationListener(
     *      javax.management.ObjectName,
     *      javax.management.NotificationListener,
     *      javax.management.NotificationFilter, Object)
     * @see JMXConnector#addConnectionNotificationListener(
     *      javax.management.NotificationListener,
     *      javax.management.NotificationFilter, Object)
     */
    protected void addNotificationListener(ObjectName name, NotificationListener l, NotificationFilter filter, Object handback) throws InstanceNotFoundException, IOException {
        if (name==null && connector!=null) {
            connector.addConnectionNotificationListener(l, filter, handback);
        } else if (name!=null) {
            connection.addNotificationListener(name, l, filter, handback);
        }
    }

    /**
     * <p>Removes a listener from a registered MBean.</p>
     *
     * <P> If the listener is registered more than once, perhaps with
     * different filters or callbacks, this method will remove all
     * those registrations.
     *
     * @param name The name of the MBean on which the listener should
     *        be removed. If null and the connector is a remote MBean
     *        server, we will stop listening listen to the connector
     *        notifications.
     * @param listener The object name of the listener to be removed.
     *
     * @exception InstanceNotFoundException The MBean name provided
     *            does not match any of the registered MBeans.
     * @exception ListenerNotFoundException The listener is not
     *            registered in the MBean.
     * @exception IOException A communication problem occurred when
     *            talking to the MBean server.
     *
     * @see MBeanServerConnection#removeNotificationListener(
     *      javax.management.ObjectName,
     *      javax.management.NotificationListener)
     * @see JMXConnector#removeConnectionNotificationListener(
     *      javax.management.NotificationListener)
     */
    void removeNotificationListener(ObjectName name, NotificationListener l) throws ListenerNotFoundException, InstanceNotFoundException, IOException {
        if (name==null && connector==null) return;

        if (name==null) {
            connector.removeConnectionNotificationListener(l);
        } else {
            connection.removeNotificationListener(name, l);
        }
    }

    void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException {
        connection.setAttribute(name, attribute);
    }

    Object getAttribute(ObjectName name, String attribute) throws InstanceNotFoundException, IOException, ReflectionException, AttributeNotFoundException, MBeanException {
        return connection.getAttribute(name, attribute);
    }

    Object invoke(ObjectName name, String operationName, Object... params) throws InstanceNotFoundException, IOException, ReflectionException, MBeanException, IntrospectionException, ClassNotFoundException, NoSatisfiableMethodsException, NoSatisfiableMBeanOperationsException, TooManySatisfiableMBeanOperationsException {
        List<String[]> signatures = new ArrayList<String[]>();
        MBeanInfo beanInfo = connection.getMBeanInfo(name);
        for (MBeanOperationInfo operationInfo : beanInfo.getOperations()) {
            if (!operationInfo.getName().equals(operationName)) continue;
            MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
            if (!checkSignatureCompatible(parameterInfos, params)) continue;

            String[] signature = new String[parameterInfos.length];
            for (int i = 0; i < signature.length; i++) {
                signature[i] = parameterInfos[i].getType();
            }
            signatures.add(signature);
        }
        if (signatures.isEmpty()) {
            throw new NoSatisfiableMBeanOperationsException(beanInfo, operationName, Arrays.asList(params));
        }
        if (signatures.size()>1){
            throw new TooManySatisfiableMBeanOperationsException(beanInfo, operationName, Arrays.asList(params), signatures);
        }
        return connection.invoke(name, operationName, params, signatures.get(0));

    }

    private boolean checkSignatureCompatible(MBeanParameterInfo[] signature, Object[] params) throws ClassNotFoundException {
        if (params==null && signature.length==0) return true;
        if (params==null || signature.length != params.length) return false;

        for (int i = 0; i < signature.length; i++) {
            Class paramClass = params[i] == null ? null : params[i].getClass();
            if (!Class.forName(signature[i].getType()).isAssignableFrom(paramClass)) {
                return false;    
            }
        }

        return true;
    }
}
