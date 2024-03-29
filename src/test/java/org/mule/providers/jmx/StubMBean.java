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

import java.util.Date;

/**
 * @author Dimitar Dimitrov
 */
public interface StubMBean {
    Date call(Date str);

    void callAsync();

    Object selectObject(int num, Object... o);

    Date returnExpected();

    Date getExpected();

    void setExpected(Date expected);

    void setAsyncExpected(Date expected);
}
