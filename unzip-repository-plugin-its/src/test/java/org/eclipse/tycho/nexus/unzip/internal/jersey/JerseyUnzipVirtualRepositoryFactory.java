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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.client.core.subsystem.repository.Repository;
import org.sonatype.nexus.client.internal.rest.jersey.subsystem.repository.JerseyVirtualRepositoryFactory;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;

@Named
@Singleton
public class JerseyUnzipVirtualRepositoryFactory extends JerseyVirtualRepositoryFactory {
    @Override
    public int canAdapt(final RepositoryBaseResource resource) {
        int score = super.canAdapt(resource);
        if (score > 0) {
            final String provider = resource.getProvider();
            if (JerseyUnzipVirtualRepository.PROVIDER.equals(provider)) {
                score++;
            }
        }
        return score;
    }

    @Override
    public JerseyUnzipVirtualRepository adapt(final JerseyNexusClient nexusClient, final RepositoryBaseResource resource) {
        return new JerseyUnzipVirtualRepository(nexusClient, (RepositoryShadowResource) resource);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canCreate(final Class<? extends Repository> type) {
        return UnzipVirtualRepository.class.equals(type);
    }

    @Override
    public JerseyUnzipVirtualRepository create(final JerseyNexusClient nexusClient, final String id) {
        return new JerseyUnzipVirtualRepository(nexusClient, id);
    }
}
