/*******************************************************************************
 * Copyright (c) 2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial implementation
 *******************************************************************************/
package org.eclipse.tycho.nexus.unzip.internal.jersey;

import org.sonatype.nexus.client.internal.rest.jersey.subsystem.repository.JerseyVirtualRepository;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;

public class JerseyUnzipVirtualRepository extends JerseyVirtualRepository<UnzipVirtualRepository> implements
        UnzipVirtualRepository {
    static final String PROVIDER = "org.eclipse.tycho.nexus.plugin.DefaultUnzipRepository";
    private static final String PROVIDER_ROLE = "org.eclipse.tycho.nexus.internal.plugin.UnzipRepository";
    private static final String REPO_TYPE = "virtual";

    public JerseyUnzipVirtualRepository(final JerseyNexusClient nexusClient, final String id) {
        super(nexusClient, id);
    }

    public JerseyUnzipVirtualRepository(final JerseyNexusClient nexusClient, final RepositoryShadowResource settings) {
        super(nexusClient, settings);
    }

    @Override
    protected RepositoryShadowResource createSettings() {
        final RepositoryShadowResource settings = new RepositoryShadowResource();
        settings.setName(settings.getId());
        settings.setRepoType(REPO_TYPE);
        settings.setProviderRole(PROVIDER_ROLE);
        settings.setProvider(PROVIDER);
        settings.setSyncAtStartup(false);
        settings.setExposed(true);

        return settings;
    }
}
