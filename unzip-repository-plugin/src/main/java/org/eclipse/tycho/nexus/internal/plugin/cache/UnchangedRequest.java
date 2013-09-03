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
package org.eclipse.tycho.nexus.internal.plugin.cache;

import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.repository.Repository;

public class UnchangedRequest extends ParsedRequest {
    public String requestPath;

    public UnchangedRequest(final String requestPath) {
        this.requestPath = requestPath;
    }

    @Override
    ConversionResult resolve(final Repository repository) throws LocalStorageException {
        return new ConversionResult(requestPath);
    }
}
