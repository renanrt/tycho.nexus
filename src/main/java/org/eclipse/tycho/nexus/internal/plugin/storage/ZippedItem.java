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
package org.eclipse.tycho.nexus.internal.plugin.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.tycho.nexus.internal.plugin.DefaultUnzipRepository;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.util.WrappingInputStream;

/**
 * This class represents an item (file or folder) within an archive file. The archive can e.g. be a
 * zip file, jar file etc.
 */
public class ZippedItem {

    /**
     * Simple utility class used to close a dedicated ZipFile on closing a provided InputStream.
     */
    private class ZipClosingEntryStream extends WrappingInputStream {

        private final ZipFile zipFile;

        public ZipClosingEntryStream(final InputStream inputStream, final ZipFile zipFile) {
            super(inputStream);
            this.zipFile = zipFile;
        }

        @Override
        public void close() throws IOException {
            super.close();
            zipFile.close();
        }

    }

    private final DefaultUnzipRepository repository;
    private final StorageItem zippedStorageItem;
    private final String pathInZip;
    private final String zipItemPath;
    private final long lastModified;
    private final Logger logger;

    /**
     * Creates a ZippedItem for a file or folder based on the path to and inside the zip file.
     * 
     * @param repository
     *            the repository in which the zipped item is accessed (the unzip repository)
     * @param zipItemPath
     *            the path to the zip file
     * @param pathInZip
     *            the path of the zipped item relative to the zip file
     * @param lastModified
     *            the modification timestamp of the zip file and all entries
     * @throws ItemNotFoundException
     *             is thrown if the path within the zip file does not point to an existing zip entry
     * @throws LocalStorageException
     *             is thrown if an issue with the zip file itself occured
     */
    public ZippedItem(final DefaultUnzipRepository repository, final String zipItemPath, final String pathInZip,
            final long lastModified, final Logger logger) throws ItemNotFoundException, LocalStorageException {
        this.repository = repository;
        this.zipItemPath = zipItemPath;

        this.lastModified = lastModified;
        this.logger = logger;

        if (pathInZip != null) {
            this.pathInZip = removeTrailingSlash(pathInZip);
        } else {
            this.pathInZip = "";
        }
        zippedStorageItem = createZippedStorageItem();
    }

    private static String removeTrailingSlash(final String path) {
        return (path.endsWith("/") ? path.substring(0, path.length() - 1) : path);
    }

    /**
     * This constructor is used to create ZippedItem objects for folders and files while browsing
     * inside a zip file.
     * 
     * @param parentItem
     *            the parent ZippedItem
     * @param entry
     *            the zip entry representing the zipped file in the zip file
     * @throws ItemNotFoundException
     */
    private ZippedItem(final ZippedItem parentItem, final ZipEntry entry, final Logger logger)
            throws ItemNotFoundException {
        repository = parentItem.getRepository();
        zipItemPath = parentItem.zipItemPath;

        lastModified = parentItem.getLastModified();
        this.logger = logger;

        if (entry.getName() != null) {
            final String pathInZip = entry.getName();
            this.pathInZip = removeTrailingSlash(pathInZip);
        } else {
            pathInZip = "";
        }
        zippedStorageItem = createZippedStorageItem(entry);
    }

    /**
     * Returns the path of the zipped item relative to the zip file. The returned path is always
     * without trailing slash (this is consistent with the behaviour of
     * {@link StorageItem#getPath()}).
     * 
     * @return the relative path of the zipped item
     */
    public String getPathInZip() {
        return pathInZip;
    }

    /**
     * Returns the absolute path of the zipped entry (path to zip + path within zip). The returned
     * path is always without trailing slash (this is consistent with the behaviour of
     * {@link StorageItem#getPath()}).
     * 
     * @return the absolute path of the zipped entry
     */
    public String getPath() {
        final String unzippedPath = zipItemPath + Util.UNZIP_TYPE_EXTENSION;
        if (pathInZip != null && !"".equals(pathInZip)) {
            return unzippedPath + "/" + pathInZip;
        } else {
            return unzippedPath;
        }
    }

    /**
     * Returns the request for this zipped item.
     * 
     * @return the request for this zipped item
     */
    public ResourceStoreRequest getRequest() {
        final ResourceStoreRequest request = new ResourceStoreRequest(getPath(), true, false);
        return request;
    }

    /**
     * Returns the repository in which this zipped item is stored (the shadow repository).
     * 
     * @return the repository in which this zipped item is stored
     */
    public DefaultUnzipRepository getRepository() {
        return repository;
    }

    /**
     * Returns the mime type for this zipped item. The mime type is determined by the file
     * extension. If a mime type cannot be determined, by default "application/octet-stream" is
     * returned. If this zipped item represents a directory <code>null</code> is returned
     * 
     * @return the mime type for this zipped item, <code>null</code> if this zipped item is a
     *         directory
     */
    public String getMimeType() {
        if (!isDirectory()) {
            return URLConnection.guessContentTypeFromName(pathInZip);
        } else {
            return null;
        }
    }

    private StorageItem createZippedStorageItem(final ZipEntry entry) {
        if (entry.isDirectory()) {
            return new ZippedStorageCollectionItem(this);
        } else {
            return new ZippedStorageFileItem(this, entry.getSize());
        }
    }

    /**
     * Creates a storage item that represents the file or folder in the zip file. If this zipped
     * item represents the zip file itself, a collection storage item for the zip file is returned.
     * 
     * @return the storage item representing the file or folder in the zip file, can be a
     *         {@link StorageFileItem} or a {@link StorageCollectionItem}, cannot be
     *         <code>null</code>
     * @throws ItemNotFoundException
     *             is thrown if the path within the zip file does not point to an existing zip entry
     * @throws LocalStorageException
     *             is thrown if an issue with the zip file itself occured
     */
    private StorageItem createZippedStorageItem() throws ItemNotFoundException, LocalStorageException {
        if (pathInZip.length() == 0) {
            // this ZippedItem represents the zip file itself
            // -> return a collection storage item for the zip file
            return new ZippedStorageCollectionItem(this);
        }
        ZipFile zipFile = null;
        try {
            final File file = repository.getCache().getArchive(zipItemPath);
            zipFile = new ZipFile(file);

            final Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final String entryName = removeTrailingSlash(entry.getName());
                if (pathInZip.equals(entryName)) {
                    return createZippedStorageItem(entry);
                }
            }
        } catch (final ItemNotFoundException e) {
            throw new LocalStorageException(e);
        } catch (final IOException e) {
            throw new LocalStorageException(e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (final IOException e) {
                    logger.warn("Unable to close ZipFile " + zipFile.getName(), e);
                }
            }
        }

        throw new ItemNotFoundException(new ResourceStoreRequest(getPath()));
    }

    /**
     * Returns the storage item that represents the zipped item. Can be a {@link StorageFileItem} or
     * a {@link StorageCollectionItem}.
     * 
     * @return the storage item that represents the zipped item, cannot be <code>null</code>
     */
    public StorageItem getZippedStorageItem() {
        return zippedStorageItem;
    }

    private boolean isDirectMember(String otherPathInZip) {
        if (otherPathInZip.endsWith("/")) {
            // cut of trailing slash
            otherPathInZip = otherPathInZip.substring(0, otherPathInZip.length() - 1);
        }
        if (pathInZip.length() == 0) {
            // this ZippedItem represents the zip file itself
            return !otherPathInZip.contains("/");
        } else {
            // this ZippedItem represents a folder within a zip file
            return otherPathInZip.startsWith(pathInZip.toString() + "/") &&
            /* limit to direct members: */otherPathInZip.indexOf("/", (pathInZip.toString() + "/").length()) == -1;
        }
    }

    /**
     * Checks whether this zipped item represents a directory.
     * 
     * @return <code>true</code> if this zipped item represents a directory, otherwise
     *         <code>false</code>
     */
    public boolean isDirectory() {
        return zippedStorageItem instanceof StorageCollectionItem;
    }

    /**
     * Returns the storage items that are direct members of this zipped item.
     * 
     * @return the storage items that are direct members of this zipped item
     * @throws LocalStorageException
     *             is thrown in case of IO errors or if it's tried to list members of a file
     * @throws ItemNotFoundException
     */
    public StorageItem[] listMembers() throws LocalStorageException, ItemNotFoundException {
        if (!isDirectory()) {
            throw new LocalStorageException("members cannot be listed for a file");
        }

        final List<StorageItem> members = new LinkedList<StorageItem>();

        final File file = repository.getCache().getArchive(zipItemPath);
        if (file.isDirectory()) {
            throw new LocalStorageException("ZipFile cannot work on directory.");
        }

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (isDirectMember(entry.getName())) {
                    StorageItem zipEntryItem;
                    final ZippedItem zippedItem = new ZippedItem(this, entry, logger);
                    if (entry.isDirectory()) {
                        zipEntryItem = new ZippedStorageCollectionItem(zippedItem);
                    } else {
                        zipEntryItem = new ZippedStorageFileItem(zippedItem, entry.getSize());
                    }
                    members.add(zipEntryItem);
                }
            }
        } catch (final IOException e) {
            throw new LocalStorageException(e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (final IOException e) {
                    logger.warn("Unable to close ZipFile " + zipFile.getName(), e);
                }
            }

        }

        return members.toArray(new StorageItem[members.size()]);
    }

    InputStream getStreamOfZippedFile() throws IOException {

        try {
            final File file = repository.getCache().getArchive(zipItemPath);
            ZipFile zipFile;

            zipFile = new ZipFile(file);

            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            final InputStream inputStream = zipFile.getInputStream(getEntry(entries));
            final ZipClosingEntryStream zipClosingEntryStream = new ZipClosingEntryStream(inputStream, zipFile);
            return zipClosingEntryStream;
        } catch (final ItemNotFoundException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private ZipEntry getEntry(final Enumeration<? extends ZipEntry> entries) {

        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            final String entryName = removeTrailingSlash(entry.getName());
            if (pathInZip.equals(entryName)) {
                return entry;
            }
        }

        return null;
    }

    public long getLastModified() {
        return lastModified;
    }

}
