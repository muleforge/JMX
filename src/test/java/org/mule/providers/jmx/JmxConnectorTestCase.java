package org.mule.providers.jmx;

import org.mule.tck.providers.AbstractConnectorTestCase;
import org.mule.umo.provider.UMOConnector;

import javax.security.auth.Subject;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * @author Dimitar Dimitrov
 */
public class JmxConnectorTestCase extends AbstractConnectorTestCase {

    public UMOConnector getConnector() throws Exception {
        JmxConnector c = new JmxConnector();
        c.initialise();
        return c;
    }

    public Object getValidMessage() throws Exception {
        return new Object[0];
    }

    public String getTestEndpointURI() {
        return "jmx:operation://domain:name1=value1,name2=value2";
    }

    public void testProperties() throws Exception {
        JmxConnector c = new JmxConnector();

        String serviceurlLocalRegistry = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";
        Subject delegationSubject = new Subject();
        Map<String,Object> env = new HashMap<String, Object>();
        env.put("environment.values", "used.by");
        env.put("the.jmx.connector", new Date());

        c.setServiceUrl(serviceurlLocalRegistry);
        assertEquals(serviceurlLocalRegistry, c.getServiceUrl());

        c.setDelegationSubject(delegationSubject);
        assertEquals(delegationSubject, c.getDelegationSubject());

        c.setEnvironment(env);
        assertEquals(env, c.getEnvironment());
    }

}
