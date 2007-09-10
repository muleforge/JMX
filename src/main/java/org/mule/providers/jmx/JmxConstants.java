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

/**
 * @author Dimitar Dimitrov
 */
public class JmxConstants {
    private JmxConstants() {}

    public static final String PROP_HANDBACK = "jmx.notification.handback";
    public static final String PROP_SEQ_NUMBER = "jmx.notification.sequenceNumber";
    public static final String PROP_TIMESTAMP = "jmx.notification.timestamp";
    public static final String PROP_TYPE = "jmx.notification.type";
    public static final String PROP_MESSAGE = "jmx.notification.message";
    public static final String PROP_SOURCE = "jmx.notification.source";
    public static final String PROP_USER_DATA = "jmx.notification.userData";
}
