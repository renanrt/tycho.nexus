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

/**
 * Result of a path conversion, that contains all relevant outcome of one conversion.
 * 
 */
public class ConversionResult {

    private final String originalPath;
    private final String convertedPath;
    private final String latestVersion;
    private final String pathUpToVersion;
    private final boolean pathConverted;
    private final boolean snapshotAvailable;

    private ConversionResult(final String originalPath, final String convertedPath, final String latestVersion,
            final String pathUpToVersion, final boolean pathConverted, final boolean snapshotAvailable) {
        this.originalPath = originalPath;
        this.convertedPath = convertedPath;
        this.latestVersion = latestVersion;
        this.pathUpToVersion = pathUpToVersion;
        this.pathConverted = pathConverted;
        this.snapshotAvailable = snapshotAvailable;
    }

    /**
     * Constructor that is called if a path was successfully converted.
     * 
     * @param originalPath
     *            the path that was requested
     * @param convertedPath
     *            the converted path, where keywords were successfully replaced
     * @param latestVersion
     *            the latest version
     * @param pathUpToVersion
     *            the path up to the index where the version starts
     */
    public ConversionResult(final String originalPath, final String convertedPath, final String latestVersion,
            final String pathUpToVersion) {
        this(originalPath, convertedPath, latestVersion, pathUpToVersion, !originalPath.equals(convertedPath), true);
    }

    /**
     * Constructor that is called if no path was converted.
     * 
     * <li/><code>getOriginalPath</code> and <code>getConvertedPath</code> will return the given
     * <code>originalPath</code>. <li/><code>getLatestVersion</code> and
     * <code>getPathUpToVersion</code> will return <code>null</code>. <li/>
     * <code>isPathConverted</code> will be <code>false</code>. <li/>
     * <code>isASnapshotAvailable</code> will be <code>true</code>.
     * 
     * @param originalPath
     *            the path that was requested
     */
    public ConversionResult(final String originalPath) {
        this(originalPath, originalPath, null, null, false, true);
    }

    /**
     * Constructor that is called if no snapshots are available.
     * 
     * <li/><code>getOriginalPath</code> and <code>getConvertedPath</code> will return the given
     * <code>originalPath</code>. <li/><code>getLatestVersion</code> will return <code>null</code>.
     * <li/><code>isPathConverted</code> will be <code>false</code>.
     * 
     * @param originalPath
     *            the path that was requested
     * @param pathUpToVersion
     *            the path up to the index where the version starts
     * @param snapshotAvailable
     *            flag that reflects if the <code>maven-metadata.xml</code> file was found and thus
     *            let us assume if there are any snapshots at all
     */
    public ConversionResult(final String originalPath, final String pathUpToVersion, final boolean snapshotAvailable) {
        this(originalPath, originalPath, null, pathUpToVersion, false, snapshotAvailable);
    }

    /**
     * 
     * @return <code>true</code> if the <code>maven-metadata.xml</code> file was found and thus let
     *         us assume if there are any snapshots at all, <code>false</code> otherwise
     */
    public boolean isASnapshotAvailable() {
        return snapshotAvailable;
    }

    /**
     * 
     * @return <code>true</code> if the requested path was converted, <code>false</code> if no
     *         conversion was performed
     */
    public boolean isPathConverted() {
        return pathConverted;
    }

    /**
     * 
     * @return the original request path
     */
    public String getOriginalPath() {
        return originalPath;
    }

    /**
     * 
     * @return the converted path, where keywords were successfully replaced or the original request
     *         path, otherwise
     */
    public String getConvertedPath() {
        return convertedPath;
    }

    /**
     * 
     * @return the latest snapshot version or <code>null</code> if no conversion took place
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * 
     * @return the path up to the index where the version starts or <code>null</code> if no
     *         conversion took place but there are snapshots available
     */
    public String getPathUpToVersion() {
        return pathUpToVersion;
    }

}
