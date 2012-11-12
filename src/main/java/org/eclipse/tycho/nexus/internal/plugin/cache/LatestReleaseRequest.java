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

import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.versioning.VersionRange;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.repository.Repository;

public class LatestReleaseRequest extends ParsedRequest {

    private final String requestPath;
    private final String groupArtifactPath;
    private final String artifactNameStart;
    private final String artifactNameEnd;
    private final VersionRange versionRange;

    public LatestReleaseRequest(final String requestPath, final String groupArtifactPath,
            final String artifactNameStart, final String artifactNameEnd, final VersionRange versionRange) {
        this.requestPath = requestPath;
        this.groupArtifactPath = groupArtifactPath;
        this.artifactNameStart = artifactNameStart;
        this.artifactNameEnd = artifactNameEnd;
        this.versionRange = versionRange;
    }

    @Override
    ConversionResult resolve(final Repository repository) throws LocalStorageException {
        try {
            final Versioning versioning = getVersioning(repository, metadataPath(groupArtifactPath + "/"));
            final String releaseVersion = versioning.getRelease();
            if (releaseVersion == null) {
                return new ConversionResult(requestPath);
            }

            final String selectedVersion = selectVersion(versioning, versionRange, false);

            final String releaseVersionDirectory = groupArtifactPath + "/" + selectedVersion + "/";

            final String pathUpToVersion = releaseVersionDirectory + artifactNameStart + "-" + selectedVersion;
            final String convertedPath = pathUpToVersion + artifactNameEnd;
            return new ConversionResult(requestPath, convertedPath, selectedVersion, pathUpToVersion);
        } catch (final ItemNotFoundException e) {
            return new ConversionResult(requestPath);
        }
    }

}
