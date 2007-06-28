package org.mule.providers.jmx;

import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.impl.endpoint.MuleEndpointURI;

/**
 * @author Dimitar Dimitrov
 */
public class JmxEndpointTestCase extends AbstractMuleTestCase {
    private static final String OBJECT_NAME = "Mule.JmxSample:type=org.mule.ManagementContext,name=MuleServerInfo";

    public void testAttribute() throws Exception {
        UMOEndpointURI url = new MuleEndpointURI("jmx:attribute://" + OBJECT_NAME + "/ThreadsRunning");
        assertUriDetails(url, "attribute", OBJECT_NAME, "ThreadsRunning");
    }

    public void testOperation() throws Exception {
        UMOEndpointURI url = new MuleEndpointURI("jmx:operation://" + OBJECT_NAME + "/dispose");
        assertUriDetails(url, "operation", OBJECT_NAME, "dispose");
    }

    public void testOperationWithSignature() throws Exception {
        UMOEndpointURI url = new MuleEndpointURI("jmx:operation://" + OBJECT_NAME + "/dispose?signature=java.lang.String;int;java.util.Date");
        assertUriDetails(url, "operation", OBJECT_NAME, "dispose");
        assertEquals("java.lang.String;int;java.util.Date", url.getParams().getProperty("signature"));
    }

    public void testConnectionNotifications() throws Exception {
        UMOEndpointURI url = new MuleEndpointURI("jmx:notification:///");
        assertUriDetails(url, "notification", null, "");
    }

    public void testFilteredConnectionNotifications() throws Exception {
        UMOEndpointURI url = new MuleEndpointURI("jmx:notification:///?filter=event1;event.2;third.type.of.event");
        assertUriDetails(url, "notification", null, "");
        assertEquals("event1;event.2;third.type.of.event", url.getParams().getProperty("filter"));
    }

    public void testMbeanNotifications() throws Exception {
        UMOEndpointURI url = new MuleEndpointURI("jmx:notification://" + OBJECT_NAME);
        assertUriDetails(url, "notification", OBJECT_NAME, null);
    }

    public void testFilteredMbeanNotifications() throws Exception {
        UMOEndpointURI url = new MuleEndpointURI("jmx:notification://" + OBJECT_NAME + "?filter=event1;event.2;third.type.of.event");
        assertUriDetails(url, "notification", OBJECT_NAME, null);
        assertEquals("event1;event.2;third.type.of.event", url.getParams().getProperty("filter"));
    }

    private void assertUriDetails(UMOEndpointURI url, String scheme, String objectName, String resource) {
        assertEquals("jmx:" + scheme, url.getFullScheme());
        assertEquals(scheme, url.getScheme());
        assertEquals(resource, url.getResourceInfo());
        assertEquals(objectName, url.getAuthority());
        assertEquals(scheme + "://" + (objectName==null? "" : objectName) + (resource == null ? "" : "/" + resource), url.getAddress());
    }
}
