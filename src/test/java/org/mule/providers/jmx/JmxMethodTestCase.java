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

import org.mule.MuleManager;
import org.mule.config.builders.QuickConfigurationBuilder;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.impl.model.direct.DirectModel;
import org.mule.routing.inbound.InboundRouterCollection;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMODescriptor;
import org.mule.umo.UMOException;
import org.mule.umo.model.UMOModel;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/** @author Dimitar Dimitrov */
abstract class JmxMethodTestCase extends FunctionalTestCase {
    static final String ONAME = "Test:type=Stub";
    Stub stub;

    protected void doPreFunctionalSetUp() throws Exception {
        ObjectName objectName = new ObjectName(ONAME);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if (mbs.isRegistered(objectName)) {
            mbs.unregisterMBean(objectName);
        }

        stub = new Stub();
        mbs.registerMBean(stub, objectName);
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
