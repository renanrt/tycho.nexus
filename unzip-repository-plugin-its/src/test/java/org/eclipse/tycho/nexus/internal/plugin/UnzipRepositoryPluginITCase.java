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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sonatype.nexus.client.core.subsystem.content.Location.repositoryLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
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

    private static final String POM_PROPERTIES_PATH_IN_ZIP = "META-INF/maven/org.example/artifact/pom.properties";

    @Parameters
    public static Collection<Object[]> data() {
        // nexus versions to be tested against
        return asList(//
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

    private static final String EXAMPLE_GROUPID = "org.example";
    private static final String EXAMPLE_ARTIFACTID = "artifact";
    private static final String EXAMPLE_RELEASED_VERSION = "1.0.1";
    private static final String EXAMPLE_SNAPSHOT_VERSION = "2.0.0-SNAPSHOT";
    private static final String EXAMPLE_SNAPSHOT_TIMESTAMP = "2.0.0-20130904.072115-1";
    private static final String EXAMPLE_PATH_PREFIX = EXAMPLE_GROUPID.replace('.', '/') + "/" + EXAMPLE_ARTIFACTID
            + "/";
    private static final String EXAMPLE_RELEASEPATH_SUFFIX = EXAMPLE_RELEASED_VERSION + "/" + EXAMPLE_ARTIFACTID + "-"
            + EXAMPLE_RELEASED_VERSION;
    private static final String EXAMPLE_SNAPSHOTPATH_SUFFIX = EXAMPLE_SNAPSHOT_VERSION + "/" + EXAMPLE_ARTIFACTID + "-"
            + EXAMPLE_SNAPSHOT_TIMESTAMP;

    private static final String EXAMPLE_RELEASED_POM = EXAMPLE_PATH_PREFIX + EXAMPLE_RELEASEPATH_SUFFIX + ".pom";
    private static final String EXAMPLE_SNAPSHOT_POM = EXAMPLE_PATH_PREFIX + EXAMPLE_SNAPSHOTPATH_SUFFIX + ".pom";
    private static final String EXAMPLE_RELEASED_JAR = EXAMPLE_PATH_PREFIX + EXAMPLE_RELEASEPATH_SUFFIX + ".jar";
    private static final String EXAMPLE_SNAPSHOT_JAR = EXAMPLE_PATH_PREFIX + EXAMPLE_SNAPSHOTPATH_SUFFIX + ".jar";
    private static final String EXAMPLE_SNAPSHOT_METADATA = EXAMPLE_PATH_PREFIX + EXAMPLE_SNAPSHOT_VERSION
            + "/maven-metadata.xml";
    private static final String EXAMPLE_MAVEN_METADATA = EXAMPLE_PATH_PREFIX + "maven-metadata.xml";

    @Before
    public void uploadExampleArtifacts() throws IOException {
        String repositoryId = "releases";
        if (!canResolveExampleArtifact(repositoryId, EXAMPLE_RELEASED_VERSION)) {
            String pathPrefix = "artifacts/releases/";
            uploadToRepository(pathPrefix, EXAMPLE_RELEASED_POM, repositoryId);
            uploadToRepository(pathPrefix, EXAMPLE_RELEASED_JAR, repositoryId);
            uploadToRepository(pathPrefix, EXAMPLE_MAVEN_METADATA, repositoryId);
            assertTrue(canResolveExampleArtifact(repositoryId, EXAMPLE_RELEASED_VERSION));
        }
        repositoryId = "snapshots";
        if (!canResolveExampleArtifact(repositoryId, EXAMPLE_SNAPSHOT_VERSION)) {
            String pathPrefix = "artifacts/snapshots/";
            uploadToRepository(pathPrefix, EXAMPLE_SNAPSHOT_POM, repositoryId);
            uploadToRepository(pathPrefix, EXAMPLE_SNAPSHOT_JAR, repositoryId);
            uploadToRepository(pathPrefix, EXAMPLE_MAVEN_METADATA, repositoryId);
            uploadToRepository(pathPrefix, EXAMPLE_SNAPSHOT_METADATA, repositoryId);
            assertTrue(canResolveExampleArtifact(repositoryId, EXAMPLE_SNAPSHOT_VERSION));
        }
    }

    @Test
    public void testUnzipRepoWithHostedRepoAsMaster() throws Exception {
        assertEquals(getTestData(EXAMPLE_RELEASED_JAR, "artifacts/releases/"),
                getFileFromZipInRepo("releases.unzip", EXAMPLE_RELEASED_JAR));
    }

    @Test
    public void testUnzipRepoWithGroupRepoAsMaster() throws Exception {
        assertEquals(getTestData(EXAMPLE_RELEASED_JAR, "artifacts/releases/"),
                getFileFromZipInRepo("releases.group.unzip", EXAMPLE_RELEASED_JAR));
    }

    @Test
    public void testUnzipRepoWithVirtualSnapshotVersion() throws Exception {
        // use virtual version "SNAPSHOT" which should translate to latest available snapshot
        assertEquals(
                getTestData(EXAMPLE_SNAPSHOT_JAR, "artifacts/snapshots/"),
                getFileFromZipInRepo("snapshots.unzip", EXAMPLE_PATH_PREFIX + "SNAPSHOT/" + EXAMPLE_ARTIFACTID
                        + "-SNAPSHOT.jar"));
    }

    private String getTestData(String localArtifactPath, String testDataPrefix) throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(testData().resolveFile(testDataPrefix + localArtifactPath));
        String expectedContent;
        try {
            ZipEntry entry = zipFile.getEntry(POM_PROPERTIES_PATH_IN_ZIP);
            expectedContent = IOUtils.toString(zipFile.getInputStream(entry));
        } finally {
            zipFile.close();
        }
        return expectedContent;
    }

    private String getFileFromZipInRepo(final String unzipRepositoryId, String zipArchivePath) throws IOException,
            FileNotFoundException {
        String pathToFileInRepo = zipArchivePath + "-unzip/" + POM_PROPERTIES_PATH_IN_ZIP;
        return getContentFromRepo(unzipRepositoryId, pathToFileInRepo);
    }

    private String getContentFromRepo(final String unzipRepositoryId, String path) throws IOException,
            FileNotFoundException {
        String downloadedContent;
        final File tempFile = File.createTempFile("test", "unzip");
        try {
            final Location loc = repositoryLocation(unzipRepositoryId, path);
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
        return downloadedContent;
    }

    private boolean canResolveExampleArtifact(String repositoryId, String version) {
        ResolveResponse resolveResponse = null;
        try {
            resolveResponse = getNexusArtifactMavenService().resolve(
                    new ResolveRequest(repositoryId, EXAMPLE_GROUPID, EXAMPLE_ARTIFACTID, version));
        } catch (final NexusClientNotFoundException e) {
            return false;
        }
        assertEquals(EXAMPLE_GROUPID, resolveResponse.getGroupId());
        assertEquals(EXAMPLE_ARTIFACTID, resolveResponse.getArtifactId());
        assertEquals("jar", resolveResponse.getExtension());
        assertEquals(version.endsWith("-SNAPSHOT"), resolveResponse.isSnapshot());
        return true;
    }
}
