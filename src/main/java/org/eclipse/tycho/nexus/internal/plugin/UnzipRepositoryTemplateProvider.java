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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplateProvider;

// Alike org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider
@Component(role = TemplateProvider.class, hint = "unzipRepo-templates")
public class UnzipRepositoryTemplateProvider extends AbstractRepositoryTemplateProvider implements Initializable {

    private static final String UNZIP_REPOSITORY_PREFIX = "unzip";

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Override
    public TemplateSet getTemplates() {
        final TemplateSet templates = new TemplateSet(null);

        final String templateId = "unzipRepo-template";
        final String templateDescription = "Unzip Repository Template";
        templates.add(new UnzipRepositoryTemplate(this, templateId, templateDescription));

        return templates;
    }

    @Override
    public void initialize() throws InitializationException {
        final RepositoryTypeDescriptor descriptor = new RepositoryTypeDescriptor(UnzipRepository.class,
                DefaultUnzipRepository.REPOSITORY_HINT, UNZIP_REPOSITORY_PREFIX, RepositoryType.UNLIMITED_INSTANCES);
        repositoryTypeRegistry.registerRepositoryTypeDescriptors(descriptor);
    }
}
