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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.proxy.repository.Repository;

abstract class ParsedRequest {
    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    abstract ConversionResult resolve(final Repository repository) throws LocalStorageException;

    Versioning getVersioning(final Repository repository, final String mdPath) throws LocalStorageException,
            ItemNotFoundException {
        final ResourceStoreRequest request = new ResourceStoreRequest(mdPath);
        final PathLock.PathLockMonitor pathLock = PathLock.getLock(mdPath);
        try {
            synchronized (pathLock) {
                final StorageItem mdItem = repository.retrieveItem(request);
                if (mdItem instanceof StorageFileItem) {
                    final InputStream is = ((StorageFileItem) mdItem).getInputStream();
                    try {
                        final Metadata md = MetadataBuilder.read(is);
                        return md.getVersioning();
                    } finally {
                        is.close();
                    }
                } else {
                    throw new LocalStorageException(mdPath + " is not an StorageFileItem in repository "
                            + repository.getId());
                }
            }
        } catch (final ItemNotFoundException e) {
            /*
             * A not existing maven-metadata.xml is assumed to indicate an not existing snapshot GAV
             * path, as there is no way to check GAV folder existence itself.
             */
            throw e;
        } catch (final Exception e) {
            throw new LocalStorageException(e);
        } finally {
            PathLock.releaseLock(pathLock);
        }
    }

    @SuppressWarnings("deprecation")
    String selectVersion(final Versioning versioning, final VersionRange versionRange, final boolean findSnapshots)
            throws ItemNotFoundException {
        // do not rely on LATEST and RELEASE tag, because they not necessarily correspond to highest version number
        String selectedVersion = getLatestVersion(versioning, findSnapshots);

        if (versionRange != null && !versionRange.containsVersion(new DefaultArtifactVersion(selectedVersion))) {
            final List<ArtifactVersion> versions = new ArrayList<ArtifactVersion>();
            for (final String artifactVersion : versioning.getVersions()) {
                if (findSnapshots || !artifactVersion.endsWith("-SNAPSHOT")) {
                    versions.add(new DefaultArtifactVersion(artifactVersion));
                }
            }
            final ArtifactVersion matchedVersion = versionRange.matchVersion(versions);
            if (matchedVersion != null) {
                selectedVersion = matchedVersion.toString();
            } else {
                throw new ItemNotFoundException("No version found within range");
            }
        }
        return selectedVersion;
    }

    @SuppressWarnings("deprecation")
    private static String getLatestVersion(final Versioning versioning, final boolean findSnapshots)
            throws ItemNotFoundException {
        final List<ArtifactVersion> artifactVersions = new ArrayList<ArtifactVersion>();
        for (final String version : versioning.getVersions()) {
            if (!findSnapshots && !version.trim().endsWith("-SNAPSHOT")) {
                artifactVersions.add(new DefaultArtifactVersion(version));
            } else if (findSnapshots) {
                artifactVersions.add(new DefaultArtifactVersion(version));
            }
        }

        ArtifactVersion maxVersion = null;
        if (artifactVersions.isEmpty()) {
            throw new ItemNotFoundException("maven-metadata.xml does not contain any version");
        }
        maxVersion = Collections.max(artifactVersions);
        return ((DefaultArtifactVersion) maxVersion).toString();
    }

    String metadataPath(final String path) {
        return path + MAVEN_METADATA_XML;
    }
}
