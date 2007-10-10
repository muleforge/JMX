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

import org.mule.config.builders.QuickConfigurationBuilder;
import org.mule.extras.client.MuleClient;
import org.mule.providers.NullPayload;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;

import java.util.Collections;
import java.util.Date;

import javax.management.Notification;

/** @author Dimitar Dimitrov */
public class MBeanAliasesTest extends JmxMethodTestCase {
    @Override
    @SuppressWarnings("unchecked")
    protected QuickConfigurationBuilder getBuilder() throws Exception {
        JmxConnector connector = new JmxConnector();
        connector.setName("JMX-Connector");
        connector.setMbeanAliases(Collections.singletonMap("stub", "Test:type=Stub"));

        QuickConfigurationBuilder builder = super.getBuilder();
        builder.getManager().registerConnector(connector);
        return builder;
    }

    public void testAttributes() throws UMOException, InterruptedException {
        Date token = new Date();
        MuleClient client = new MuleClient();
        assertNull(stub.getExpected());
        assertEquals(0, stub.awaitAsyncCall(0, 1000));

        client.dispatch("jmx:attribute://stub/AsyncExpected", token, null);
        assertEquals(1, stub.awaitAsyncCall(1, 1000));
        assertEquals(token, stub.getExpected());

        client.dispatch("jmx:attribute://stub/AsyncExpected", null, null);
        stub.awaitAsyncCall(2, 1000);
        assertNull(stub.getExpected());

        client.dispatch("jmx:attribute://stub/AsyncExpected", new Date(0), null);
        stub.awaitAsyncCall(3, 1000);
        assertEquals(new Date(0), stub.getExpected());

        UMOMessage response = client.send("jmx:attribute://stub/Expected", token, null);
        assertTrue((Boolean) response.getPayload());
        assertEquals(token, stub.getExpected());

        response = client.send("jmx:attribute://stub/Expected", null, null);
        assertTrue((Boolean) response.getPayload());
        assertEquals(null, stub.getExpected());

        response = client.send("jmx:attribute://stub/Expected", new Date(1), null);
        assertTrue((Boolean) response.getPayload());
        assertEquals(new Date(1), stub.getExpected());

        stub.setDisableUpdates(true);
        response = client.send("jmx:attribute://stub/Expected", token, null);
        assertFalse((Boolean) response.getPayload());
        assertEquals(new Date(1), stub.getExpected());

        response = client.send("jmx:attribute://stub/Expected", token, null);
        assertFalse((Boolean) response.getPayload());
        assertEquals(new Date(1), stub.getExpected());

        response = client.send("jmx:attribute://stub/Expected", null, null);
        assertFalse((Boolean) response.getPayload());
        assertEquals(new Date(1), stub.getExpected());

        stub.setDisableUpdates(false);
        response = client.send("jmx:attribute://stub/Expected", token, null);
        assertTrue((Boolean) response.getPayload());
        assertEquals(token, stub.getExpected());

        stub.setExpected(token);
        response = client.receive("jmx:attribute://stub/Expected", 1000);
        assertSame(token, response.getPayload());

        stub.setExpected(null);
        response = client.receive("jmx:attribute://stub/Expected", 1000);
        assertEquals(NullPayload.getInstance(), response.getPayload());

        stub.setExpected(new Date(0));
        response = client.receive("jmx:attribute://stub/Expected", 1000);
        assertEquals(new Date(0), response.getPayload());
    }

    public void testOperations() throws UMOException {
        Date expected = new Date();
        stub.setExpected(expected);
        MuleClient client = new MuleClient();
        assertEquals(1, stub.getVersion());
        UMOMessage response = client.send("jmx:operation://stub/call", new Object[]{expected}, null);
        assertEquals(2, stub.getVersion());
        assertEquals(response.getPayload(), expected);
        response = client.send("jmx:operation://stub/call", new Object[]{new Date(100)}, null);
        assertEquals(3, stub.getVersion());
        assertEquals(NullPayload.getInstance(), response.getPayload());
    }

    public void testNotifications() throws UMOException, InterruptedException {
        NotificationReceiverStub receiver = registerSingletonComponent("Listener", "jmx:notification://stub?types=foo;bar&handback=100&whatever...", NotificationReceiverStub.class);

        stub.sendNotification(new Notification("foo", this, 2000));
        assertEquals("foo", receiver.retreive(100).getType());

        stub.sendNotification(new Notification("qux", this, 2001));
        try {
            fail("Received unexpected notification: " + receiver.retreive(100));
        } catch (InterruptedException e) {
            logger.debug("Expected exception: " + e);
        }

        stub.sendNotification(new Notification("qux.foo", this, 2002));
        try {
            fail("Received unexpected notification: " + receiver.retreive(100));
        } catch (InterruptedException e) {
            logger.debug("Expected exception: " + e);
        }

        stub.sendNotification(new Notification("bar.qux", this, 2004));
        assertEquals("bar.qux", receiver.retreive(100).getType());

        stub.sendNotification(new Notification("bar", this, 2005));
        assertEquals("bar", receiver.retreive(100).getType());
    }
}
