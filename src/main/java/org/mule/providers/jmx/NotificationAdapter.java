package org.mule.providers.jmx;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.provider.MessageTypeNotSupportedException;

import javax.management.Notification;

/**
 * @author Dimitar Dimitrov
 * TODO: externalize propnames to a special JMXConstants class
 *       @link org.mule.providers.jms.JMSConstants.
 */
class NotificationAdapter extends AbstractMessageAdapter {
    private final Notification notification;

    public NotificationAdapter(Object message) throws MessageTypeNotSupportedException {
        if (!(message instanceof Notification)) {
            throw new MessageTypeNotSupportedException(message, getClass());
        }
        this.notification = (Notification) message;
        id = String.valueOf(getSequenceNumber());
        setLongProperty("jmx.notification.sequenceNumber", notification.getSequenceNumber());
        setLongProperty("jmx.notification.timestamp", notification.getTimeStamp());
        setStringProperty("jmx.notification.type", notification.getType());
        setStringProperty("jmx.notification.message", notification.getMessage());
        setProperty("jmx.notification.source", notification.getSource());
        setProperty("jmx.notification.userData", notification.getUserData());
    }

    public String getPayloadAsString(String encoding) throws Exception {
        return notification.toString();
    }

    public byte[] getPayloadAsBytes() throws Exception {
        return convertToBytes(getUserData());
    }

    public Object getPayload() {
        return notification;
    }

    public String getType() {
        return notification.getType();
    }

    public long getSequenceNumber() {
        return notification.getSequenceNumber();
    }

    public long getTimeStamp() {
        return notification.getTimeStamp();
    }

    public String getMessage() {
        return notification.getMessage();
    }

    public Object getUserData() {
        return notification.getUserData();
    }

    public Object getSource() {
        return notification.getSource();
    }
}
