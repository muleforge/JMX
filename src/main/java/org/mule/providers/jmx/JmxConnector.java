package org.mule.providers.jmx;

import org.mule.providers.AbstractConnector;
import org.mule.providers.DefaultMessageAdapter;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.UMOException;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.UMOMessageAdapter;

import javax.management.*;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnector;
import javax.security.auth.Subject;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Arrays;
import java.io.IOException;

/**
 * jmx:Domain:type=Object,id=Instance/property/State
 * jmx:Domain:type=Object,id=Instance/event/Filter=*
 * jmx:Domain:type=Object,id=Instance/action/Method
 * jmx:connector/event
 */
public class JmxConnector extends AbstractConnector {
    public static final String URIPROP_FILTER_NOTIFTYPE = "types";
    public static final String URIPROP_FILTER_ATTRIBUTES = "attributes";
    public static final String PROP_HANDBACK = "jmx.notification.handback";
    public static final String PROP_SEQ_NUMBER = "jmx.notification.sequenceNumber";
    public static final String PROP_TIMESTAMP = "jmx.notification.timestamp";
    public static final String PROP_TYPE = "jmx.notification.type";
    public static final String PROP_MESSAGE = "jmx.notification.message";
    public static final String PROP_SOURCE = "jmx.notification.source";
    public static final String PROP_USER_DATA = "jmx.notification.userData";

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



    void addNotificationListener(ObjectName name, NotificationListener l, NotificationFilter filter, Object handback) throws InstanceNotFoundException, IOException {
        connection.addNotificationListener(name, l, filter, handback);
    }

    void removeNotificationListener(ObjectName name, NotificationListener l) throws ListenerNotFoundException, InstanceNotFoundException, IOException {
        connection.removeNotificationListener(name, l);
    }

    Object invoke(ObjectName name, String operationName, Object... params) throws InstanceNotFoundException, IOException, ReflectionException, MBeanException, IntrospectionException, ClassNotFoundException {
        return connection.invoke(name, operationName, params, guessSignature(name, operationName, params));
    }

    void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException {
        connection.setAttribute(name, attribute);
    }

    Object getAttribute(ObjectName name, String attribute) throws InstanceNotFoundException, IOException, ReflectionException, AttributeNotFoundException, MBeanException {
        return connection.getAttribute(name, attribute);
    }

    private String[] guessSignature(ObjectName name, String operationName, Object[] params) throws IntrospectionException, InstanceNotFoundException, IOException, ReflectionException, ClassNotFoundException {
        MBeanInfo beanInfo = connection.getMBeanInfo(name);
        for (MBeanOperationInfo operationInfo : beanInfo.getOperations()) {
            if (!operationInfo.getName().equals(operationName)) continue;
            MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
            if (!checkSignatureCompatible(parameterInfos, params)) continue;

            String[] signature = new String[parameterInfos.length];
            for (int i = 0; i < signature.length; i++) {
                signature[i] = parameterInfos[i].getType();
            }

            return signature;
        }
        throw new RuntimeException("did not guess the signature"); // TODO: add proper exception
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
