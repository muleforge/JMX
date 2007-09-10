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

import org.mule.extras.client.MuleClient;
import org.mule.providers.NullPayload;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;

import java.util.Date;

/**
 * @author Dimitar Dimitrov
 */
public class JmxAttributeTestCase extends JmxMethodTestCase {
    public void testDispatch() throws UMOException, InterruptedException {
        Date token = new Date();
        MuleClient client = new MuleClient();
        assertNull(stub.getExpected());
        assertEquals(0, stub.awaitAsyncCall(0, 1000));

        client.dispatch("jmx:attribute://Test:type=Stub/AsyncExpected", token, null);
        assertEquals(1, stub.awaitAsyncCall(1, 1000));
        assertEquals(token, stub.getExpected());

        client.dispatch("jmx:attribute://Test:type=Stub/AsyncExpected", null, null);
        stub.awaitAsyncCall(2, 1000);
        assertNull(stub.getExpected());

        client.dispatch("jmx:attribute://Test:type=Stub/AsyncExpected", new Date(0), null);
        stub.awaitAsyncCall(3, 1000);
        assertEquals(new Date(0), stub.getExpected());
    }

    public void testReceive() throws UMOException {
        Date token = new Date();
        MuleClient client = new MuleClient();

        stub.setExpected(token);
        UMOMessage response = client.receive("jmx:attribute://Test:type=Stub/Expected", 1000);
        assertSame(token, response.getPayload());

        stub.setExpected(null);
        response = client.receive("jmx:attribute://Test:type=Stub/Expected", 1000);
        assertEquals(NullPayload.getInstance(), response.getPayload());

        stub.setExpected(new Date(0));
        response = client.receive("jmx:attribute://Test:type=Stub/Expected", 1000);
        assertEquals(new Date(0), response.getPayload());
    }

    public void testSend() throws UMOException {
        Date token = new Date();
        MuleClient client = new MuleClient();

        UMOMessage response = client.send("jmx:attribute://Test:type=Stub/Expected", token, null);
        assertTrue((Boolean) response.getPayload());
        assertEquals(token, stub.getExpected());

        response = client.send("jmx:attribute://Test:type=Stub/Expected", null, null);
        assertTrue((Boolean) response.getPayload());
        assertEquals(null, stub.getExpected());

        response = client.send("jmx:attribute://Test:type=Stub/Expected", new Date(1), null);
        assertTrue((Boolean) response.getPayload());
        assertEquals(new Date(1), stub.getExpected());

        stub.setDisableUpdates(true);
        response = client.send("jmx:attribute://Test:type=Stub/Expected", token, null);
        assertFalse((Boolean) response.getPayload());
        assertEquals(new Date(1), stub.getExpected());

        response = client.send("jmx:attribute://Test:type=Stub/Expected", token, null);
        assertFalse((Boolean) response.getPayload());
        assertEquals(new Date(1), stub.getExpected());

        response = client.send("jmx:attribute://Test:type=Stub/Expected", null, null);
        assertFalse((Boolean) response.getPayload());
        assertEquals(new Date(1), stub.getExpected());

        stub.setDisableUpdates(false);
        response = client.send("jmx:attribute://Test:type=Stub/Expected", token, null);
        assertTrue((Boolean) response.getPayload());
        assertEquals(token, stub.getExpected());
    }
}
