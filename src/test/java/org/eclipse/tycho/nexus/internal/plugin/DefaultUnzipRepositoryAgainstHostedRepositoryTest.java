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

import java.io.IOException;

import org.eclipse.tycho.nexus.internal.plugin.storage.Util;
import org.eclipse.tycho.nexus.internal.plugin.storage.ZipAwareStorageCollectionItem;
import org.eclipse.tycho.nexus.internal.plugin.test.RepositoryMock;
import org.eclipse.tycho.nexus.internal.plugin.test.TestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.util.ItemPathUtils;

/**
 * Tests the DefaultUnzipRepository with a hosted repository as master repository.
 */
@SuppressWarnings("nls")
public class DefaultUnzipRepositoryAgainstHostedRepositoryTest extends DefaultUnzipRepositoryTest {

    @Override
    protected RepositoryMock createRepositoryMock() {
        return RepositoryMock.createMasterRepo();
    }

    @Test
    public void testRetrieveCollection() throws ItemNotFoundException, IllegalOperationException, IOException,
            AccessDeniedException, NoSuchResourceStoreException {
        final String collectionPath = "/dir";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(collectionPath));
        Assert.assertTrue(item instanceof ZipAwareStorageCollectionItem);
        final StorageCollectionItem collectionItem = (StorageCollectionItem) item;
        Assert.assertEquals(collectionPath, collectionItem.getPath());
        TestUtil.assertMembers(new String[] { "/dir/subdir" }, new String[0], collectionItem.list());
    }

    @Test
    public void testRetrieveCollectionWithTrailingSlash() throws ItemNotFoundException, IllegalOperationException,
            IOException, AccessDeniedException, NoSuchResourceStoreException {
        final String collectionPath = "/dir/";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(collectionPath));
        Assert.assertTrue(item instanceof ZipAwareStorageCollectionItem);
        final StorageCollectionItem collectionItem = (StorageCollectionItem) item;
        Assert.assertEquals(ItemPathUtils.cleanUpTrailingSlash(collectionPath), collectionItem.getPath());
        TestUtil.assertMembers(new String[] { "/dir/subdir" }, new String[0], collectionItem.list());
    }

    @Test
    public void testRetrieveCollectionWithArchiveMember() throws Exception {
        final String collectionPath = "/dir/subdir";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(collectionPath));
        Assert.assertTrue(item instanceof ZipAwareStorageCollectionItem);
        final StorageCollectionItem collectionItem = (StorageCollectionItem) item;
        Assert.assertEquals(collectionPath, collectionItem.getPath());
        TestUtil.assertMembers( //
                new String[] { "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION, //
                        "/dir/subdir/archive2.zip" + Util.UNZIP_TYPE_EXTENSION },//
                new String[0], //
                collectionItem.list());
    }

    @Test
    public void testRetrieveCollectionWithArchiveMemberWithTrailingSlash() throws Exception {
        final String collectionPath = "/dir/subdir/";
        final StorageItem item = unzipRepo.doRetrieveItem(new ResourceStoreRequest(collectionPath));
        Assert.assertTrue(item instanceof ZipAwareStorageCollectionItem);
        final StorageCollectionItem collectionItem = (StorageCollectionItem) item;
        Assert.assertEquals(ItemPathUtils.cleanUpTrailingSlash(collectionPath), collectionItem.getPath());
        TestUtil.assertMembers( //
                new String[] { "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION, //
                        "/dir/subdir/archive2.zip" + Util.UNZIP_TYPE_EXTENSION },//
                new String[0], collectionItem.list());
    }

    @AfterClass
    public static void classTearDown() {
        TestUtil.cleanUpTestFiles();
    }
}
