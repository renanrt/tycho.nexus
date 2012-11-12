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

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepositoryConfiguration;

public class UnzipRepositoryConfiguration extends AbstractShadowRepositoryConfiguration {

    private static final String USE_VIRTUAL_VERSION = "useVirtualVersion";

    public UnzipRepositoryConfiguration(final Xpp3Dom configuration) {
        super(configuration);
    }

    public boolean isUseVirtualVersion() {
        return Boolean.parseBoolean(getNodeValue(getRootNode(), USE_VIRTUAL_VERSION, Boolean.FALSE.toString()));
    }

    public void setUseVirtualVersion(final boolean val) {
        setNodeValue(getRootNode(), USE_VIRTUAL_VERSION, Boolean.toString(val));
    }
}
