package org.mule.providers.jmx;

import org.mule.tck.FunctionalTestCase;
import org.mule.config.ConfigurationBuilder;
import org.mule.config.builders.QuickConfigurationBuilder;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.components.simple.BridgeComponent;
import org.mule.routing.inbound.InboundRouterCollection;
import org.mule.routing.inbound.SelectiveConsumer;
import org.mule.umo.routing.UMOInboundRouter;
import org.mule.umo.lifecycle.Stoppable;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.extras.client.MuleClient;
import org.mule.providers.NullPayload;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Date;
import java.lang.management.ManagementFactory;

/**
 * @author Dimitar Dimitrov
 */
public class JmxOperationTestCase extends FunctionalTestCase {
    private Stub stub;
    private Object expected = new Date();
    private static final String ONAME = "Test:type=Stub";

    protected void doPreFunctionalSetUp() throws Exception {
        stub = new Stub(expected);
        ManagementFactory.getPlatformMBeanServer().registerMBean(stub, new ObjectName(ONAME));
    }

    protected void doFunctionalTearDown() throws Exception {
        super.doFunctionalTearDown();
        ObjectName objectName = new ObjectName(ONAME);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if (mbs.isRegistered(objectName)) {
            mbs.unregisterMBean(objectName);
        }
    }

    protected String getConfigResources() {
        return null;
    }

    protected ConfigurationBuilder getBuilder() throws Exception {
        QuickConfigurationBuilder builder = new QuickConfigurationBuilder();
        return builder;
    }

    public void testSimpleInvocation() throws UMOException {
        MuleClient client = new MuleClient();
        assertEquals(0, stub.getCalled());
        UMOMessage response = client.send("jmx:operation://Test:type=Stub/call", new Object[]{expected}, null);
        assertEquals(1, stub.getCalled());
        assertEquals(response.getPayload(), expected);
        response = client.send("jmx:operation://Test:type=Stub/call", new Object[]{"something else"}, null);
        assertEquals(2, stub.getCalled());
        assertEquals(NullPayload.getInstance(), response.getPayload());
    }
}
