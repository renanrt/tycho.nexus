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
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplateProvider;

//Alike org.sonatype.nexus.templates.repository.maven.Maven1Maven2ShadowRepositoryTemplate
public class UnzipRepositoryTemplate extends AbstractRepositoryTemplate {

    public UnzipRepositoryTemplate(final AbstractRepositoryTemplateProvider provider, final String id,
            final String description) {
        super(provider, id, description, new Maven2ContentClass(), UnzipRepository.class);
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration() {
        final CRepository repo = new DefaultCRepository();

        repo.setId("");
        repo.setName("");
        repo.setProviderRole(UnzipRepository.class.getName());
        repo.setProviderHint(DefaultUnzipRepository.REPOSITORY_HINT);

        final Xpp3Dom ex = new Xpp3Dom(DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME);
        repo.setExternalConfiguration(ex);

        final UnzipRepositoryConfiguration exConf = new UnzipRepositoryConfiguration(ex);
        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy(RepositoryWritePolicy.READ_ONLY.name());

        final CRepositoryCoreConfiguration result = new CRepositoryCoreConfiguration(getTemplateProvider()
                .getApplicationConfiguration(), repo,
                new CRepositoryExternalConfigurationHolderFactory<UnzipRepositoryConfiguration>() {
                    public UnzipRepositoryConfiguration createExternalConfigurationHolder(final CRepository config) {
                        return new UnzipRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
                    }
                });

        return result;
    }

}
