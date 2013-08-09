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

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.versioning.VersionRange;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

public class RequestPathConverter {
    private static final Pattern SNAPSHOT_PATTERN = Pattern
            .compile("^(.*/(?:\\d*\\.?)+(?:-\\w+)*-SNAPSHOT/)([^/]*-)SNAPSHOT[^/]");
    private static final Pattern LATESTVERSION_PATTERN = Pattern.compile("/SNAPSHOT/([^/]*)-SNAPSHOT[^/]");
    private static final Pattern RELEASE_PATTERN = Pattern.compile("/RELEASE/([^/]*)-RELEASE[^/]");

    /**
     * Converts the path to an artifact. The following requests are possible:
     * <ul>
     * <li>Latest released version, requested with the special version keyword <code>RELEASE</code></li>
     * <li>Latest existing version (including SNAPSHOTs), requested with the special version keyword
     * <code>SNAPSHOT</code></li>
     * <li>The latest build identifier for a given SNAPSHOT-version, requested with the version
     * <code>x.y.z-SNAPSHOT</code></li>
     * </ul>
     * Returns an unchanged path in case the provided path does not match the request structure.
     * 
     * @param repository
     *            A <code>Repository</code>, where the requested artifact is searched for.
     * @param request
     *            The request to be resolved to an specific artifact.
     * @param useVirtualVersions
     *            whether the keywords SNAPSHOT or RELEASE shall be evaluated
     * @return The result object of a dynamic version conversion.
     * 
     * @throws LocalStorageException
     * @throws IllegalRequestException
     *             if the range parameter of the request url cannot be parsed according Maven
     *             version range spec
     */
    public static ConversionResult convert(final Repository repository, final ResourceStoreRequest request,
            final boolean useVirtualVersions) throws LocalStorageException, IllegalRequestException {
        final ParsedRequest parsedRequest = parseRequest(request, useVirtualVersions);
        return parsedRequest.resolve(repository);
    }

    private static ParsedRequest parseRequest(final ResourceStoreRequest request, final boolean useVirtualVersions) {
        final String requestPath = request.getRequestPath();

        if (useVirtualVersions) {
            final Matcher snapshotVersionMatcher = SNAPSHOT_PATTERN.matcher(requestPath);
            if (snapshotVersionMatcher.find()) {
                final MatchResult versionFolderMatchResult = snapshotVersionMatcher.toMatchResult();
                final String pathToSnapshotArtifact = versionFolderMatchResult.group(1);
                final String pathUpToVersion = pathToSnapshotArtifact + versionFolderMatchResult.group(2);
                final String artifactNameEnd = requestPath.substring(versionFolderMatchResult.end() - 1);
                return new SnapshotRequest(requestPath, pathUpToVersion, pathToSnapshotArtifact, artifactNameEnd);
            }
            final Matcher latestVersionMatcher = LATESTVERSION_PATTERN.matcher(requestPath);
            if (latestVersionMatcher.find()) {

                final MatchResult matchResult = latestVersionMatcher.toMatchResult();
                final String groupArtifactPath = requestPath.substring(0, matchResult.start());
                final String artifactNameStart = matchResult.group(1);
                final String artifactNameEnd = requestPath.substring(matchResult.end() - 1);

                final VersionRange versionRange = parseVersionRange(request);

                return new LatestVersionRequest(request, groupArtifactPath, artifactNameStart, artifactNameEnd,
                        versionRange);
            }

            final Matcher releaseVersionMatcher = RELEASE_PATTERN.matcher(requestPath);
            if (releaseVersionMatcher.find()) {
                final MatchResult matchResult = releaseVersionMatcher.toMatchResult();
                final String groupArtifactPath = requestPath.substring(0, matchResult.start());
                final String artifactNameStart = matchResult.group(1);
                final String artifactNameEnd = requestPath.substring(matchResult.end() - 1);
                final VersionRange versionRange = parseVersionRange(request);
                return new LatestReleaseRequest(request, groupArtifactPath, artifactNameStart, artifactNameEnd,
                        versionRange);
            }
        }
        return new UnchangedRequest(requestPath);
    }

    private static VersionRange parseVersionRange(final ResourceStoreRequest request) {
        // TODO: create versionRange form requestUrl. 
        return null;
    }
}
