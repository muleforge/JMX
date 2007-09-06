package org.mule.providers.jmx;

import org.mule.extras.client.MuleClient;
import org.mule.providers.NullPayload;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;

import java.util.Date;

/**
 * @author Dimitar Dimitrov
 */
public class JmxOperationTestCase extends JmxMethodTestCase {
    public void testDispatch() throws UMOException, InterruptedException {
        MuleClient client = new MuleClient();
        assertEquals(0, stub.getVersion());
        client.dispatch("jmx:operation://Test:type=Stub/callAsync", new Object[0], null);

        assertEquals(1, stub.awaitAsyncCall(1, 1000));
        client.dispatch("jmx:operation://Test:type=Stub/callAsync", new Object[0], null);
        assertEquals(2, stub.awaitAsyncCall(2, 1000));
    }

    public void testReceive() throws UMOException, InterruptedException {
        MuleClient client = new MuleClient();
        assertEquals(0, stub.getVersion());

        stub.setExpected(new Date(0));
        UMOMessage response = client.receive("jmx:operation://Test:type=Stub/returnExpected", 1000);
        assertEquals(new Date(0), response.getPayload());
        assertEquals(2, stub.awaitAsyncCall(1, 1000));


        stub.setExpected(new Date(1));
        response = client.receive("jmx:operation://Test:type=Stub/returnExpected", 1000);
        assertEquals(new Date(1), response.getPayload());
        assertEquals(4, stub.awaitAsyncCall(2, 1000));
    }

    public void testSend() throws UMOException {
        Date expected = new Date();
        stub.setExpected(expected);
        MuleClient client = new MuleClient();
        assertEquals(1, stub.getVersion());
        UMOMessage response = client.send("jmx:operation://Test:type=Stub/call", new Object[]{expected}, null);
        assertEquals(2, stub.getVersion());
        assertEquals(response.getPayload(), expected);
        response = client.send("jmx:operation://Test:type=Stub/call", new Object[]{new Date(100)}, null);
        assertEquals(3, stub.getVersion());
        assertEquals(NullPayload.getInstance(), response.getPayload());
    }
}
