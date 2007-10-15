package org.mule.config.i18n;

public class MessageFactoryHack extends MessageFactory {
    protected static Message createMessageX(String bundlePath, int code, Object... arguments) {
        String messageString = getString(bundlePath, code, arguments);
        return new Message(messageString, 0, arguments);
    }

    protected static Message createMessageY(String bundlePath, int code, Object... arguments) {
        return createMessageX(bundlePath, code, arguments);
    }
}
