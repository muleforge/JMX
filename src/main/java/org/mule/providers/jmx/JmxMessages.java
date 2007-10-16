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

import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactoryHack;
import org.mule.util.StringMessageUtils;

import java.util.List;

import javax.management.MBeanInfo;

public class JmxMessages extends MessageFactoryHack {
    private static final String BUNDLE_PATH = getBundlePath("jmx");

    public static Message noSatisfiableOperations(MBeanInfo beanInfo, String operationName, List<Object> params) {
        return createMessage(BUNDLE_PATH, 2,
                beanInfo,
                operationName,
                StringMessageUtils.toString(params)
        );
    }

    public static Message tooManySatisfiableOperations(MBeanInfo beanInfo, String operationName, List<Object> params, List<String[]> signatures) {
        return createMessageY(BUNDLE_PATH, 3,
                beanInfo,
                operationName,
                StringMessageUtils.toString(params),
                StringMessageUtils.toString(signatures)
        );
    }

    public static Message primitiveArraysNotSupported() {
        return createMessage(BUNDLE_PATH, -2);
    }

    public static Object ignoringRemoveListenerOnPlatformMBS() {
        return createMessage(BUNDLE_PATH, 4);
    }
}
