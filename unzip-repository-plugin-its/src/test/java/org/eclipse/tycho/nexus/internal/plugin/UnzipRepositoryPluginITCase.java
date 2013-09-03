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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sonatype.nexus.client.core.subsystem.content.Location.repositoryLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.artifact.ResolveRequest;
import org.sonatype.nexus.client.core.subsystem.artifact.ResolveResponse;
import org.sonatype.nexus.client.core.subsystem.content.Location;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy.Strategy;

@NexusStartAndStopStrategy(Strategy.EACH_TEST)
public class UnzipRepositoryPluginITCase extends AbstractUnzipRepositoryPluginITCase {

    @Parameters
    public static Collection<Object[]> data() {
        // nexus versions to be tested against
        return Arrays.asList(//
                new Object[] { getTestProperty("nexus.min.coords") }, //
                new Object[] { getTestProperty("nexus.max.coords") });
    }

    private static String getTestProperty(String key) {
        Properties testProps = new Properties();
        InputStream stream = UnzipRepositoryPluginITCase.class.getResourceAsStream("/test.properties");
        try {
            testProps.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return testProps.getProperty(key);
    }

    public UnzipRepositoryPluginITCase(String nexusBundleCoordinates) {
        super(nexusBundleCoordinates);
    }

    private static final String EXAMPLE_POM = "org/example/artifact/1.0.1/artifact-1.0.1.pom";
    private static final String EXAMPLE_JAR = "org/example/artifact/1.0.1/artifact-1.0.1.jar";
    private static final String EXAMPLE_MAVEN_METADATA = "org/example/artifact/maven-metadata.xml";

    @Before
    public void uploadExampleArtifacts() throws IOException {
        if (!canResolveExampleArtifact()) {
            uploadToReleasesRepository(EXAMPLE_POM);
            uploadToReleasesRepository(EXAMPLE_JAR);
            uploadToReleasesRepository(EXAMPLE_MAVEN_METADATA);
            assertTrue(canResolveExampleArtifact());
        }
    }

    @Test
    public void testUnzipRepoWithHostedRepoAsMaster() throws Exception {
        assertDownloadExampleZipEntryFromUnzipRepository("releases.unzip");
    }

    @Test
    public void testUnzipRepoWithGroupRepoAsMaster() throws Exception {
        assertDownloadExampleZipEntryFromUnzipRepository("releases.group.unzip");
    }

    private void assertDownloadExampleZipEntryFromUnzipRepository(final String unzipRepositoryId) throws IOException {
        final String pathInZip = "META-INF/maven/org.example/artifact/pom.properties";
        String downloadedContent;
        final File tempFile = File.createTempFile("test", "unzip");
        try {
            final Location loc = repositoryLocation(unzipRepositoryId, EXAMPLE_JAR + "-unzip/" + pathInZip);
            getNexusContentService().download(loc, tempFile);
            FileInputStream stream = new FileInputStream(tempFile);
            try {
                downloadedContent = IOUtils.toString(stream);
            } finally {
                stream.close();
            }
        } finally {
            tempFile.delete();
        }
        ZipFile zipFile = new ZipFile(testData().resolveFile("artifacts/" + EXAMPLE_JAR));
        String expectedContent;
        try {
            ZipEntry entry = zipFile.getEntry(pathInZip);
            expectedContent = IOUtils.toString(zipFile.getInputStream(entry));
        } finally {
            zipFile.close();
        }
        assertEquals(expectedContent, downloadedContent);
    }

    private boolean canResolveExampleArtifact() {
        ResolveResponse resolveResponse = null;
        try {
            resolveResponse = getNexusArtifactMavenService().resolve(
                    new ResolveRequest("releases", "org.example", "artifact", ResolveRequest.VERSION_RELEASE));
        } catch (final NexusClientNotFoundException e) {
            // if it's not there yet... that's o.k.
        }
        return resolveResponse != null //
                && "org.example".equals(resolveResponse.getGroupId()) //
                && "artifact".equals(resolveResponse.getArtifactId()) //
                && "jar".equals(resolveResponse.getExtension()) //
                && !resolveResponse.isSnapshot();
    }
}
