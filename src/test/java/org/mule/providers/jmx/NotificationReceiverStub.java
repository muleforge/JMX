package org.mule.providers.jmx;

import javax.management.Notification;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author Dimitar Dimitrov
 */
public class NotificationReceiverStub {
    private final LinkedBlockingDeque<Notification> notificationPostbox = new LinkedBlockingDeque<Notification>();

    public void receive(Notification n) {
        notificationPostbox.add(n);
    }

    public Notification retreive(int timeout) throws InterruptedException {
        long start = System.currentTimeMillis();
        Notification notification = notificationPostbox.poll((long) (timeout * 1.5), TimeUnit.MILLISECONDS);
        if (start+ timeout < System.currentTimeMillis()) {
            throw new InterruptedException("Timed out: " + timeout + "ms.");
        }
        return notification;
    }
}
