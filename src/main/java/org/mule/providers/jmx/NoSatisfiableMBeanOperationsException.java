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

import org.mule.config.i18n.MessageFactory;
import org.mule.umo.UMOException;
import org.mule.util.StringMessageUtils;

import java.util.List;

import javax.management.MBeanInfo;

public class NoSatisfiableMBeanOperationsException extends UMOException {
    public NoSatisfiableMBeanOperationsException(MBeanInfo beanInfo, String operationName, List<Object> params) {
        super(MessageFactory.createStaticMessage(beanInfo + " -> " + operationName + " (" + StringMessageUtils.toString(params) + " )"));        
    }

}
