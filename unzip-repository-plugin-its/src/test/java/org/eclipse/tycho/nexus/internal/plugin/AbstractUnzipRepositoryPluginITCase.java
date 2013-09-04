/*******************************************************************************
 * Copyright (c) 2013 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial implementation
 *******************************************************************************/
package org.eclipse.tycho.nexus.internal.plugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.sonatype.nexus.client.core.subsystem.content.Location.repositoryLocation;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.junit.Before;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.artifact.ArtifactMaven;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.sisu.filetasks.FileTaskBuilder;
import org.sonatype.sisu.filetasks.builder.FileRef;

public abstract class AbstractUnzipRepositoryPluginITCase extends NexusRunningParametrizedITSupport {

    // set this to true if you want to debug the startup phase of nexus
    private static final Boolean SUSPEND_ON_START = false;
    private static final Integer DEBUG_PORT = 8000;
    private static final String NEXUS_LOG_LEVEL = "DEBUG";

    @Inject
    private FileTaskBuilder fileTaskBuilder;

    public AbstractUnzipRepositoryPluginITCase(final String nexusBundleCoordinates) {
        super(nexusBundleCoordinates);
    }

    @Before
    public void setup() {
        assertTrue(nexus().isRunning());
        assertThat(client().getNexusStatus().getEditionLong(), notNullValue());
    }

    @Override
    protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
        final File nexusXmlOverlay = testData().resolveFile("preset-nexus");
        configuration.addOverlays(fileTaskBuilder.copy().directory(FileRef.file(nexusXmlOverlay)).to()
                .directory(FileRef.path("sonatype-work/nexus/conf")));
        configuration.setLogLevel(NEXUS_LOG_LEVEL);
        configuration.enableDebugging(DEBUG_PORT, SUSPEND_ON_START);
        final File unzipPlugin = artifactResolver().resolveFromDependencyManagement("org.eclipse.tycho.nexus",
                "unzip-repository-plugin", null, null, "zip", "bundle");
        configuration.addPlugins(unzipPlugin).setLogLevel(NEXUS_LOG_LEVEL);
        return configuration;
    }

    protected void uploadToRepository(String testDataPathPrefix, final String artifactPath, String repositoryId)
            throws IOException {
        final File resolveFile = testData().resolveFile(testDataPathPrefix + artifactPath);
        getNexusContentService().upload(repositoryLocation(repositoryId, artifactPath), resolveFile);
    }

    protected Content getNexusContentService() {
        return client().getSubsystem(Content.class);
    }

    protected ArtifactMaven getNexusArtifactMavenService() {
        return client().getSubsystem(ArtifactMaven.class);
    }

}
