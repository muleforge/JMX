package org.mule.providers.jmx;

import org.mule.umo.UMOException;

import javax.management.Notification;

/**
 * @author Dimitar Dimitrov
 */
public class JmxNotificationTestCase extends JmxMethodTestCase {
    private static final int JMX_TIMEOUT = 100;
    public void testReceiveForBean() throws UMOException, InterruptedException {
        NotificationReceiverStub receiver = registerSingletonComponent("Listener", "jmx:notification://" + ONAME, NotificationReceiverStub.class);

        long timestamp = System.currentTimeMillis();
        Notification n = new Notification("test.foo", this, 2000, timestamp, "User Message");
        n.setUserData(stub);

        stub.sendNotification(n);

        Notification notification = receiver.retreive(JMX_TIMEOUT);
        assertEquals("test.foo", notification.getType());
        assertEquals(2000, notification.getSequenceNumber());
        assertEquals(this, notification.getSource());
        assertEquals(timestamp, notification.getTimeStamp());
        assertEquals("User Message", notification.getMessage());
        assertEquals(stub, notification.getUserData());
    }
                          
    public void testReceiveFiltered() throws UMOException, InterruptedException {
        NotificationReceiverStub receiver = registerSingletonComponent("Listener", "jmx:notification://" + ONAME + "?types=foo;bar&handback=100&whatever...", NotificationReceiverStub.class);

        stub.sendNotification(new Notification("foo", this, 2000));
        assertEquals("foo", receiver.retreive(JMX_TIMEOUT).getType());

        stub.sendNotification(new Notification("qux", this, 2001));
        try {
            fail("Received unexpected notification: " + receiver.retreive(JMX_TIMEOUT));
        } catch (InterruptedException e) {
            logger.debug("Expected exception: " + e);
        }

        stub.sendNotification(new Notification("qux.foo", this, 2002));
        try {
            fail("Received unexpected notification: " + receiver.retreive(JMX_TIMEOUT));
        } catch (InterruptedException e) {
            logger.debug("Expected exception: " + e);
        }

        stub.sendNotification(new Notification("bar.qux", this, 2004));
        assertEquals("bar.qux", receiver.retreive(JMX_TIMEOUT).getType());

        stub.sendNotification(new Notification("bar", this, 2005));
        assertEquals("bar", receiver.retreive(JMX_TIMEOUT).getType());
    }

    // TODO: test notification handbacks
}
