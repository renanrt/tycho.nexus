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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.tycho.nexus.internal.plugin.storage.Util;
import org.junit.Test;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;

@SuppressWarnings({ "deprecation" })
public class RequestPathConverterTest {

    private static final String PATH_TO_CORRECT_MAVEN_METADATA_XML = "./src/test/resources/maven-metadata.xml";
    private static final String PATH_TO_MISSING_SNAPSHOT_MAVEN_METADATA_XML = "./src/test/resources/missingSnapshot-maven-metadata.xml";
    private static final String PATH_TO_MISSING_VERSIONING_MAVEN_METADATA_XML = "./src/test/resources/missingVersioning-maven-metadata.xml";
    private static final String PATH_TO_NOT_EXISTING_MAVEN_METADATA_XML = "./src/test/resources/no-maven-metadata.xml";
    private static final String VALID_VERSION_QUALIFIER_OF_TESTRESOURCE = "20100505.133931-1";

    @Test
    public void testSnapshotZipToLatestShapshotZipConversion() throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "");
    }

    @Test
    public void testSnapshotZipToLatestShapshotZipConversionWithClassifier() throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "", "x.y-z");
    }

    @Test
    public void testSnapshotZipToLatestShapshotZipConversionWithTrailingSlash() throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "/");
    }

    @Test
    public void testSnapshotZipToLatestShapshotZipConversionWithTrailingSlashAndClassifier() throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "/", "x.y.z");
    }

    @Test
    public void testDirectoryInSnapshotZipToDirectoryInLatestShapshotZipConversion() throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "/plugins");
    }

    @Test
    public void testDirectoryInSnapshotZipToDirectoryInLatestShapshotZipConversionWithClassifier() throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "/plugins", "x-y-z");
    }

    @Test
    public void testDirectoryInSnapshotZipToDirectoryInLatestShapshotZipConversionWithTrailingSlash() throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "/plugins/");
    }

    @Test
    public void testDirectoryInSnapshotZipToDirectoryInLatestShapshotZipConversionWithTrailingSlashWithClassifier()
            throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "/plugins/", "x-y.z");
    }

    @Test
    public void testDirectoryInSnapshotZipToDirectoryInLatestShapshotZipConversionWithSnapshotInContentName()
            throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE,
                "/plugins/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.jar");
    }

    @Test
    public void testDirectoryInSnapshotZipToDirectoryInLatestShapshotZipConversionWithClassifierAndSnapshotInContentName()
            throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE,
                "/plugins/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.jar", "assembly");
    }

    @Test
    public void testDirectoryInSnapshotZipToDirectoryInLatestShapshotZipConversionWithBetaQualifierWithClassifierAndSnapshotInContentName()
            throws Exception {
        assertPathConversion("0.1.0-BETA-1-SNAPSHOT", "0.1.0-BETA-1-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE,
                "/plugins/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.jar", "assembly");
    }

    @Test
    public void testFileInSnapshotZipToFileInLatestShapshotZipConversion() throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE,
                "/plugins/myplugin.jar");
    }

    @Test
    public void testFileInSnapshotZipToFileInLatestShapshotZipConversionWithClassifier() throws Exception {
        assertPathConversion("0.1.0-SNAPSHOT", "0.1.0-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE,
                "/plugins/myplugin.jar", "assembly");
    }

    @Test
    public void testSnapshotZipToLatestShapshotZipConversionWithoutPointReleaseVersion() throws Exception {
        assertPathConversion("0.1-SNAPSHOT", "0.1-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "");
    }

    @Test
    public void testSnapshotZipToLatestShapshotZipConversionWithoutPointReleaseVersionWithClassifier() throws Exception {
        assertPathConversion("0.1-SNAPSHOT", "0.1-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "", "xyz");
    }

    @Test
    public void testSnapshotZipToLatestShapshotZipConversionWithMultipleDigitVersionParts() throws Exception {
        assertPathConversion("11.222.3333-SNAPSHOT", "11.222.3333-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "");
    }

    @Test
    public void testSnapshotZipToLatestShapshotZipConversionWithMultipleDigitVersionPartsWithClassifier()
            throws Exception {
        assertPathConversion("11.222.3333-SNAPSHOT", "11.222.3333-" + VALID_VERSION_QUALIFIER_OF_TESTRESOURCE, "",
                "xyx");
    }

    @Test
    public void testNoConversionForNonSnapshotVersionButSimilar() throws Exception {
        final String path = "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-MYSNAPSHOT/org.eclipse.tycho.example.updatesite-0.1.0-MYSNAPSHOT.zip";
        final ConversionResult conversionResult = RequestPathConverter.convert(
                createRepositoryMock(null, PATH_TO_CORRECT_MAVEN_METADATA_XML), new ResourceStoreRequest(path), true);
        Assert.assertFalse(conversionResult.isPathConverted());
        Assert.assertEquals(path, conversionResult.getConvertedPath());
    }

    @Test
    public void testNoConversionForNonSnapshotVersionWithoutPointRelease() throws Exception {
        final String path = "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1-BETA/org.eclipse.tycho.example.updatesite-0.1-BETA.zip";
        final ConversionResult conversionResult = RequestPathConverter.convert(
                createRepositoryMock(null, PATH_TO_CORRECT_MAVEN_METADATA_XML), new ResourceStoreRequest(path), true);
        Assert.assertFalse(conversionResult.isPathConverted());
        Assert.assertEquals(path, conversionResult.getConvertedPath());
    }

    @Test
    public void testNoConversionForNonSnapshotVersionWithSnapshotIdInContent() throws Exception {
        final String path = "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-BETA/org.eclipse.tycho.example.updatesite-0.1.0-BETA.zip/plugins/org.eclipse.tycho.example.updatesite-SNAPSHOT.jar";
        final ConversionResult conversionResult = RequestPathConverter.convert(
                createRepositoryMock(null, PATH_TO_CORRECT_MAVEN_METADATA_XML), new ResourceStoreRequest(path), true);
        Assert.assertFalse(conversionResult.isPathConverted());
        Assert.assertEquals(path, conversionResult.getConvertedPath());
    }

    @Test
    public void testNoConversionForNonSnapshotVersionWithSnapshotIdInArtifactIdandInContent() throws Exception {
        final String path = "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-BETA/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.zip/plugins/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.jar";
        final ConversionResult conversionResult = RequestPathConverter.convert(
                createRepositoryMock(null, PATH_TO_CORRECT_MAVEN_METADATA_XML), new ResourceStoreRequest(path), true);
        Assert.assertFalse(conversionResult.isPathConverted());
        Assert.assertEquals(path, conversionResult.getConvertedPath());
    }

    @Test
    public void testNoConversionForSnapshotVersionWithNonSnapshotIdInArtifactIdandSnapshotInContent() throws Exception {
        final String path = "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-SNAPSHOT/org.eclipse.tycho.example.updatesite-0.1.0-20100505.zip/plugins/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.jar";
        final ConversionResult conversionResult = RequestPathConverter.convert(
                createRepositoryMock(null, PATH_TO_CORRECT_MAVEN_METADATA_XML), new ResourceStoreRequest(path), true);
        Assert.assertFalse(conversionResult.isPathConverted());
        Assert.assertEquals(path, conversionResult.getConvertedPath());
    }

    @Test
    public void testNoConversionBecauseOfMissingSnapshotTagInMetadataXml() throws Exception {
        final String path = "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-SNAPSHOT/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.zip/plugins/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.jar";
        try {
            RequestPathConverter.convert(
                    createRepositoryMock(
                            "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-SNAPSHOT/",
                            PATH_TO_MISSING_SNAPSHOT_MAVEN_METADATA_XML), new ResourceStoreRequest(path), true);
            Assert.fail("StorageException expected");
        } catch (final StorageException e) {
            Assert.assertTrue(e.getMessage().contains("current"));
        }
    }

    @Test
    public void testNoConversionBecauseOfMissingVersioningTagInMetadataXml() throws Exception {
        final String path = "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-SNAPSHOT/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.zip/plugins/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.jar";
        try {
            RequestPathConverter.convert(
                    createRepositoryMock(
                            "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-SNAPSHOT/",
                            PATH_TO_MISSING_VERSIONING_MAVEN_METADATA_XML), new ResourceStoreRequest(path), true);
            Assert.fail("StorageException expected");
        } catch (final StorageException e) {
            Assert.assertTrue(e.getMessage().contains("versioning"));
        }
    }

    @Test(expected = StorageException.class)
    public void testNoConversionBecauseOfNotExistingMetadataXml() throws Exception {
        final String path = "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-SNAPSHOT/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.zip/plugins/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.jar";
        RequestPathConverter.convert(
                createRepositoryMock("org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-SNAPSHOT/",
                        PATH_TO_NOT_EXISTING_MAVEN_METADATA_XML), new ResourceStoreRequest(path), true);
    }

    @Test
    public void testNoMetaDataFound() throws Exception {
        final String path = "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-SNAPSHOT/org.eclipse.tycho.example.updatesite-0.1.0-SNAPSHOT.zip";
        final MavenRepository repository = createRepositoryMockNotFindingMetaData("org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/0.1.0-SNAPSHOT/");
        final ConversionResult conversionResult = RequestPathConverter.convert(repository, new ResourceStoreRequest(
                path), true);
        Assert.assertFalse(conversionResult.isASnapshotAvailable());
    }

    /**
     * 
     * @param snapshotVersion
     *            String that will be converted to an existing version number during path
     *            conversion.
     * @param timestampVersion
     *            The version number that shall be used instead of the snapshot version string. The
     *            given time stamp version has to be the same like specified in the
     *            <code>maven-metadata.xml</code> file in the resources folder.
     * @param pathInZip
     *            Path to a specific file inside the archive, relative to the archive root. If not
     *            empty a leading slash is needed.
     * @param classifier
     *            String that describes the content of an artifact and is added to the artifact name
     *            as suffix of the version, separated by '<code>-</code>'. In case that no
     *            classification is required, an alternative method without this parameter can be
     *            used.
     * @throws IllegalArtifactCoordinateException
     * @throws IOException
     * @throws ItemNotFoundException
     */
    private static void assertPathConversion(final String snapshotVersion, final String timestampVersion,
            final String pathInZip, final String classifier) throws Exception {
        String appliedClassifier = "";
        if (classifier.length() > 0) {
            appliedClassifier = "-" + classifier;
        }
        final String parentPath = "org/eclipse/tycho/example/org.eclipse.tycho.example.updatesite/" + snapshotVersion
                + "/";
        final String snapshotFileName = "org.eclipse.tycho.example.updatesite-" + snapshotVersion + appliedClassifier
                + ".zip" + Util.UNZIP_TYPE_EXTENSION;
        final String snapshotPath = parentPath + snapshotFileName + pathInZip;
        final String expectedLatestSnapshotPath = parentPath
                + snapshotFileName.replace(snapshotVersion, timestampVersion) + pathInZip;
        final ConversionResult conversionResult = RequestPathConverter.convert(
                createRepositoryMock(parentPath, PATH_TO_CORRECT_MAVEN_METADATA_XML), new ResourceStoreRequest(
                        snapshotPath), true);
        Assert.assertTrue(conversionResult.isPathConverted());
        Assert.assertEquals(expectedLatestSnapshotPath, conversionResult.getConvertedPath());
    }

    /**
     * 
     * @param snapshotVersion
     *            String that will be converted to an existing version number during path
     *            conversion.
     * @param timestampVersion
     *            The version number that shall be used instead of the snapshot version string. The
     *            given time stamp version has to be the same like specified in the
     *            <code>maven-metadata.xml</code> file in the resources folder.
     * @param pathInZip
     *            Path to a specific file inside the archive, relative to the archive root. If not
     *            empty a leading slash is needed.
     * @throws Exception
     */
    private static void assertPathConversion(final String snapshotVersion, final String timestampVersion,
            final String pathInZip) throws Exception {
        assertPathConversion(snapshotVersion, timestampVersion, pathInZip, "");
    }

    @SuppressWarnings("unchecked")
    private static MavenRepository createRepositoryMock(final String pathToArtifact, final String pathToTestMetadata) {
        final String pathToMetadataXML = pathToArtifact + "maven-metadata.xml";
        final MavenRepository repositoryMock = EasyMock.createMock(MavenRepository.class);
        final Capture<ResourceStoreRequest>[] captures = new Capture[] { new Capture<ResourceStoreRequest>() };
        try {
            EasyMock.expect(repositoryMock.getId()).andStubReturn("");
            EasyMock.expect(repositoryMock.retrieveItem(EasyMock.capture(captures[0]))).andAnswer(
                    new IAnswer<StorageFileItem>() {
                        @Override
                        public StorageFileItem answer() throws Throwable {
                            final StorageFileItem mavenMetaDataXml = EasyMock.createMock(StorageFileItem.class);
                            EasyMock.expect(mavenMetaDataXml.getInputStream()).andAnswer(new IAnswer<InputStream>() {
                                @Override
                                public InputStream answer() throws Throwable {
                                    Assert.assertEquals(pathToMetadataXML, captures[0].getValue().getRequestPath());
                                    final File metaDataFile = new File(pathToTestMetadata);
                                    return new FileInputStream(metaDataFile);
                                }
                            });
                            EasyMock.replay(mavenMetaDataXml);
                            return mavenMetaDataXml;
                        }
                    });
        } catch (final Exception e) {
            throw new RuntimeException("Unable to create Maven Repo Mock", e);
        }
        EasyMock.replay(repositoryMock);
        return repositoryMock;
    }

    private static MavenRepository createRepositoryMockNotFindingMetaData(final String pathToArtifact) {
        final String pathToMetadataXML = pathToArtifact + "maven-metadata.xml";
        final MavenRepository repositoryMock = EasyMock.createMock(MavenRepository.class);
        try {
            EasyMock.expect(repositoryMock.getId()).andStubReturn("");
            EasyMock.expect(repositoryMock.retrieveItem((ResourceStoreRequest) EasyMock.anyObject())).andThrow(
                    new ItemNotFoundException(new ResourceStoreRequest(pathToMetadataXML)));
        } catch (final Exception e) {
            throw new RuntimeException("Unable to create Maven Repo Mock", e);
        }
        EasyMock.replay(repositoryMock);
        return repositoryMock;
    }
}
