/*******************************************************************************
 * Copyright (c) 2010, 2012 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.tycho.nexus.internal.plugin;

public class RequestTimeTrace {

    private final long startTime;

    private final String requestPath;

    public RequestTimeTrace(final String requestPath) {
        this.startTime = System.currentTimeMillis();
        this.requestPath = requestPath;
    }

    public long getTimeSpent() {
        return System.currentTimeMillis() - startTime;
    }

    public String getMessage() {
        return "Served request in " + getTimeSpent() + "ms: " + requestPath;
    }
}
