package org.mule.providers.jmx;

import org.mule.tck.providers.AbstractMessageAdapterTestCase;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.umo.MessagingException;

import javax.management.Notification;
import java.util.ArrayList;

/**
 * @author Dimitar Dimitrov
 */
public class JmxNotificationAdapterTestCase extends AbstractMessageAdapterTestCase {
    public UMOMessageAdapter createAdapter(Object payload) throws MessagingException {
        return new NotificationAdapter(payload);
    }

    public Object getValidMessage() throws Exception {
        Notification notification = new Notification("type", this, System.nanoTime(), System.currentTimeMillis(), "test notification");
        notification.setUserData(new ArrayList());
        return notification;
    }
}
