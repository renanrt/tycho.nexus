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
package org.eclipse.tycho.nexus.internal.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.tycho.nexus.internal.plugin.storage.Util;
import org.eclipse.tycho.nexus.internal.plugin.storage.ZippedStorageCollectionItem;
import org.eclipse.tycho.nexus.internal.plugin.test.RepositoryMock;
import org.eclipse.tycho.nexus.internal.plugin.test.TestUtil;
import org.eclipse.tycho.nexus.internal.plugin.test.UnzipRepositoryMock;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.util.ItemPathUtils;

public abstract class DefaultUnzipRepositoryTest {
    protected final RepositoryMock repositoryMock;
    protected final DefaultUnzipRepository unzipRepo;

    public DefaultUnzipRepositoryTest() {
        repositoryMock = createRepositoryMock();
        unzipRepo = UnzipRepositoryMock.createUnzipRepository(repositoryMock);
    }

    protected abstract RepositoryMock createRepositoryMock();

    @Test
    public void testGetRepositoryKind() {
        final DefaultUnzipRepository repo = new DefaultUnzipRepository();
        final RepositoryKind kind = repo.getRepositoryKind();
        assertEquals(UnzipRepository.class, kind.getMainFacet());
        assertTrue(kind.isFacetAvailable(ShadowRepository.class));
        assertFalse(kind.isFacetAvailable(HostedRepository.class));
    }

    @Test
    public void testRetrieveFile() throws ItemNotFoundException, IllegalOperationException, IOException {
        final String filePath = "/dir/a.txt";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(filePath));
        Assert.assertTrue(item instanceof DefaultStorageFileItem);
        final DefaultStorageFileItem fileItem = (DefaultStorageFileItem) item;
        Assert.assertEquals("text/plain", fileItem.getMimeType());
        Assert.assertEquals(filePath, fileItem.getPath());
        TestUtil.assertContent("content of a.txt", fileItem);
    }

    @Test(expected = ItemNotFoundException.class)
    public void testRetrieveNonExistingItem() throws ItemNotFoundException, IllegalOperationException, IOException {
        unzipRepo.doRetrieveItem(new ResourceStoreRequest("/x.txt"));
    }

    @Test
    public void testRetrieveArchiveAsFile() throws ItemNotFoundException, IllegalOperationException, IOException {
        final String archivePath = "/dir/subdir/archive.zip";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(archivePath));
        Assert.assertTrue(item instanceof DefaultStorageFileItem);
        final DefaultStorageFileItem fileItem = (DefaultStorageFileItem) item;
        Assert.assertEquals("application/zip", fileItem.getMimeType());
        Assert.assertEquals(archivePath, fileItem.getPath());
    }

    @Test
    public void testRetrieveArchiveAsCollection() throws ItemNotFoundException, IllegalOperationException, IOException,
            AccessDeniedException, NoSuchResourceStoreException {
        final String archivePath = "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION;
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(archivePath));
        Assert.assertTrue(item instanceof ZippedStorageCollectionItem);
        final ZippedStorageCollectionItem archiveItem = (ZippedStorageCollectionItem) item;
        Assert.assertEquals(ItemPathUtils.cleanUpTrailingSlash(archivePath), archiveItem.getPath());
        TestUtil.assertMembers(new String[] { archivePath + "/dir" }, new String[] { archivePath + "/test.txt" },
                archiveItem.list());
    }

    @Test
    public void testRetriveFileInArchive() throws IllegalOperationException, ItemNotFoundException, IOException {
        final String filePath = "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/test.txt";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(filePath));
        Assert.assertTrue(item instanceof DefaultStorageFileItem);
        final DefaultStorageFileItem fileItem = (DefaultStorageFileItem) item;
        Assert.assertEquals("text/plain", fileItem.getMimeType());
        Assert.assertEquals(filePath, fileItem.getPath());
        TestUtil.assertContent("some content", fileItem);
    }

    @Test
    public void testRetrieveCollectionInArchive() throws IllegalOperationException, ItemNotFoundException,
            AccessDeniedException, NoSuchResourceStoreException, IOException {
        final String collectionPath = "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/dir";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(collectionPath));
        Assert.assertTrue(item instanceof ZippedStorageCollectionItem);
        final StorageCollectionItem collectionItem = (StorageCollectionItem) item;
        Assert.assertEquals(collectionPath, collectionItem.getPath());
        final Collection<StorageItem> members = collectionItem.list();
        TestUtil.assertMembers(new String[] { collectionPath + "/subdir" },
                new String[] { collectionPath + "/test.txt" }, members);
        final DefaultStorageFileItem fileItem = (DefaultStorageFileItem) TestUtil.findItem(
                collectionPath + "/test.txt", members);
        TestUtil.assertContent("some file content", fileItem);
    }

    @Test
    public void testRetrieveCollectionInArchiveWithTrailingSlash() throws IllegalOperationException,
            ItemNotFoundException, AccessDeniedException, NoSuchResourceStoreException, IOException {
        final String collectionPath = "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/dir/";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(collectionPath));
        Assert.assertTrue(item instanceof ZippedStorageCollectionItem);
        final StorageCollectionItem collectionItem = (StorageCollectionItem) item;
        Assert.assertEquals(ItemPathUtils.cleanUpTrailingSlash(collectionPath), collectionItem.getPath());
        final Collection<StorageItem> members = collectionItem.list();
        TestUtil.assertMembers(new String[] { collectionPath + "subdir" },
                new String[] { collectionPath + "test.txt" }, members);
        final DefaultStorageFileItem fileItem = (DefaultStorageFileItem) TestUtil.findItem(collectionPath + "test.txt",
                members);
        TestUtil.assertContent("some file content", fileItem);
    }

    @Test
    public void testRetrieveSubCollectionInArchive() throws IllegalOperationException, ItemNotFoundException,
            AccessDeniedException, NoSuchResourceStoreException, IOException {
        final String collectionPath = "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/dir/subdir";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(collectionPath));
        Assert.assertTrue(item instanceof ZippedStorageCollectionItem);
        final StorageCollectionItem collectionItem = (StorageCollectionItem) item;
        Assert.assertEquals(collectionPath, collectionItem.getPath());
        final Collection<StorageItem> members = collectionItem.list();
        TestUtil.assertMembers(new String[] {}, new String[] { collectionPath + "/a.txt" }, members);
        final DefaultStorageFileItem fileItem = (DefaultStorageFileItem) TestUtil.findItem(collectionPath + "/a.txt",
                members);
        TestUtil.assertContent("some more content", fileItem);
    }

    @Test
    public void testRetrieveSubCollectionInArchiveWithTrailingSlash() throws IllegalOperationException,
            ItemNotFoundException, AccessDeniedException, NoSuchResourceStoreException, IOException {
        final String collectionPath = "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/dir/subdir/";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(collectionPath));
        Assert.assertTrue(item instanceof ZippedStorageCollectionItem);
        final StorageCollectionItem collectionItem = (StorageCollectionItem) item;
        Assert.assertEquals(ItemPathUtils.cleanUpTrailingSlash(collectionPath), collectionItem.getPath());
        final Collection<StorageItem> members = collectionItem.list();
        TestUtil.assertMembers(new String[] {}, new String[] { collectionPath + "a.txt" }, members);
        final DefaultStorageFileItem fileItem = (DefaultStorageFileItem) TestUtil.findItem(collectionPath + "a.txt",
                members);
        TestUtil.assertContent("some more content", fileItem);
    }

    @Test(expected = ItemNotFoundException.class)
    public void testRetrieveNonExistingItemInArchive() throws LocalStorageException, IllegalOperationException,
            ItemNotFoundException {
        unzipRepo.doRetrieveItem(new ResourceStoreRequest("/dir/subdir/archive.zip/x.txt"));
    }

    @Test(expected = ItemNotFoundException.class)
    public void testRetrieveNonExistingItemInFolderInArchive() throws LocalStorageException, IllegalOperationException,
            ItemNotFoundException {
        unzipRepo.doRetrieveItem(new ResourceStoreRequest("/dir/subdir/archive.zip/dir/x.txt"));
    }

    @Test
    public void testCreateLink() throws LocalStorageException, UnsupportedStorageOperationException,
            IllegalOperationException {
        unzipRepo.createLink(null);
    }

    @Test
    public void testDeleteLink() throws LocalStorageException, UnsupportedStorageOperationException,
            IllegalOperationException, ItemNotFoundException {
        unzipRepo.deleteLink(null);
    }

}
