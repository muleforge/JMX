package org.mule.providers.jmx;

import org.mule.tck.FunctionalTestCase;
import org.mule.config.builders.QuickConfigurationBuilder;
import org.mule.impl.model.direct.DirectModel;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.impl.MuleDescriptor;
import org.mule.umo.UMOException;
import org.mule.umo.UMODescriptor;
import org.mule.umo.model.UMOModel;
import org.mule.MuleManager;
import org.mule.routing.inbound.InboundRouterCollection;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

/**
 * @author Dimitar Dimitrov
 */
abstract class JmxMethodTestCase extends FunctionalTestCase {
    static final String ONAME = "Test:type=Stub";
    Stub stub;

    protected void doPreFunctionalSetUp() throws Exception {
        stub = new Stub();
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

    protected QuickConfigurationBuilder getBuilder() throws Exception {
        QuickConfigurationBuilder builder = new QuickConfigurationBuilder();
        builder.disableAdminAgent();
        builder.registerModel(new DirectModel());
        return builder;
    }

    @SuppressWarnings({"unchecked"})
            <T> T registerSingletonComponent(String name, String uri, Class<T> clazz) throws UMOException {
        UMOModel m = (UMOModel) MuleManager.getInstance().getModels().values().iterator().next();
        InboundRouterCollection inbound = new InboundRouterCollection();
        inbound.addEndpoint(new MuleEndpoint(uri, true));
        UMODescriptor descriptor = new MuleDescriptor();
        descriptor.setSingleton(true);
        descriptor.setInboundRouter(inbound);
        descriptor.setName(name);
        descriptor.setImplementation(clazz.getName());
        m.registerComponent(descriptor);
        return (T) m.getComponent(name).getInstance();
    }
}
