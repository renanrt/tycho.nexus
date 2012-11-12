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

import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.repository.Repository;

public class SnapshotRequest extends ParsedRequest {
    public String requestPath;
    public String pathUpToVersion;
    public String pathToSnapshotArtifact;
    private final String artifactNameEnd;

    public SnapshotRequest(final String requestPath, final String pathUpToVersion, final String pathToSnapshotArtifact,
            final String artifactNameEnd) {
        this.requestPath = requestPath;
        this.pathUpToVersion = pathUpToVersion;
        this.pathToSnapshotArtifact = pathToSnapshotArtifact;
        this.artifactNameEnd = artifactNameEnd;
    }

    @Override
    ConversionResult resolve(final Repository repository) throws LocalStorageException {
        String latestSnapshotVersion;

        try {
            latestSnapshotVersion = getLatestSnapshotVersion(repository);
        } catch (final ItemNotFoundException e) {
            return new ConversionResult(requestPath, pathUpToVersion, false);
        }

        if (latestSnapshotVersion != null) {
            final String pathToVersionedArtifact = pathUpToVersion + latestSnapshotVersion + artifactNameEnd;
            return new ConversionResult(requestPath, pathToVersionedArtifact, latestSnapshotVersion, pathUpToVersion);
        } else {
            return new ConversionResult(requestPath);
        }
    }

    private String getLatestSnapshotVersion(final Repository repository) throws LocalStorageException,
            ItemNotFoundException {
        final String mdPath = metadataPath(pathToSnapshotArtifact);
        final Versioning mdVersioning = getVersioning(repository, mdPath);
        if (mdVersioning != null) {
            final Snapshot current = mdVersioning.getSnapshot();
            if (current != null) {
                return current.getTimestamp() + "-" + current.getBuildNumber();
            } else {
                throw new LocalStorageException(mdPath + " does not contain current information in repository "
                        + repository.getId());
            }
        } else {
            throw new LocalStorageException(mdPath + " does not contain versioning information in repository "
                    + repository.getId());
        }
    }
}
