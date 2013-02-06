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
import java.util.ArrayList;

import org.eclipse.tycho.nexus.internal.plugin.DefaultUnzipRepository;
import org.eclipse.tycho.nexus.internal.plugin.test.RepositoryMock;
import org.eclipse.tycho.nexus.internal.plugin.test.TestUtil;
import org.eclipse.tycho.nexus.internal.plugin.test.UnzipRepositoryMock;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

@SuppressWarnings("nls")
public class ZipAwareStorageCollectionItemTest {

    protected Logger testLogger = LoggerFactory.getLogger(getClass());

    @Test
    public void testListWithArchiveMember() throws ItemNotFoundException, IOException, AccessDeniedException,
            NoSuchResourceStoreException, IllegalOperationException {
        final RepositoryMock masterRepository = RepositoryMock.createMasterRepo();
        final DefaultUnzipRepository unzipRepositoryMock = UnzipRepositoryMock.createUnzipRepository(masterRepository);
        final StorageCollectionItem collectionStorageItem = (StorageCollectionItem) masterRepository
                .createStorageItem("/dir/subdir");
        final ZipAwareStorageCollectionItem zipAwareStorageCollectionItem = new ZipAwareStorageCollectionItem(
                unzipRepositoryMock, collectionStorageItem, testLogger);
        TestUtil.assertMembers( //
                new String[] { "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION, //
                        "/dir/subdir/archive2.zip" + Util.UNZIP_TYPE_EXTENSION },//
                new String[0], //
                zipAwareStorageCollectionItem.list());
    }

    @Test
    public void testListWithSnapshotArchives() throws Exception {
        final RepositoryMock masterRepository = RepositoryMock.createSnapshotRepo();
        final DefaultUnzipRepository unzipRepositoryMock = UnzipRepositoryMock.createUnzipRepository(masterRepository);
        final StorageCollectionItem collectionStorageItem = (StorageCollectionItem) masterRepository
                .createStorageItem("/ga/1.0.0-SNAPSHOT");
        final ZipAwareStorageCollectionItem zipAwareStorageCollectionItem = new ZipAwareStorageCollectionItem(
                unzipRepositoryMock, collectionStorageItem, testLogger);
        TestUtil.assertMembers(new String[] {
                "/ga/1.0.0-SNAPSHOT/archive-1.0.0-SNAPSHOT-juhu.zip" + Util.UNZIP_TYPE_EXTENSION,
                "/ga/1.0.0-SNAPSHOT/archive-1.0.0-SNAPSHOT.zip" + Util.UNZIP_TYPE_EXTENSION }, new String[0],
                zipAwareStorageCollectionItem.list());

    }

    @Test
    public void testModificationTimes() throws ItemNotFoundException, IOException, AccessDeniedException,
            NoSuchResourceStoreException, IllegalOperationException {
        final RepositoryMock masterRepository = RepositoryMock.createMasterRepo();
        final DefaultUnzipRepository unzipRepositoryMock = UnzipRepositoryMock.createUnzipRepository(masterRepository);
        final StorageCollectionItem collectionStorageItem = (StorageCollectionItem) masterRepository
                .createStorageItem("/dir/subdir");
        final ZipAwareStorageCollectionItem zipAwareStorageCollectionItem = new ZipAwareStorageCollectionItem(
                unzipRepositoryMock, collectionStorageItem, testLogger);
        final ArrayList<StorageItem> items = new ArrayList<StorageItem>(zipAwareStorageCollectionItem.list());
        System.out.println(items.get(0).getPath());
        System.out.println(items.get(0).getModified());
        final File archiveFile = new File("./src/test/resources/masterRepo/dir/subdir/archive.zip");
        final File archive2File = new File("./src/test/resources/masterRepo/dir/subdir/archive2.zip");

        Assert.assertEquals(archiveFile.lastModified(), items.get(0).getModified());
        Assert.assertEquals(archive2File.lastModified(), items.get(1).getModified());
    }

    @Test
    public void testListWithoutArchiveMember() throws ItemNotFoundException, IOException, AccessDeniedException,
            NoSuchResourceStoreException, IllegalOperationException {
        final RepositoryMock masterRepository = RepositoryMock.createMasterRepo();
        final DefaultUnzipRepository unzipRepositoryMock = UnzipRepositoryMock.createUnzipRepository(masterRepository);
        final StorageCollectionItem collectionStorageItem = (StorageCollectionItem) masterRepository
                .createStorageItem("/dir");
        final ZipAwareStorageCollectionItem zipAwareStorageCollectionItem = new ZipAwareStorageCollectionItem(
                unzipRepositoryMock, collectionStorageItem, testLogger);
        TestUtil.assertMembers(new String[] { "/dir/subdir" }, new String[0], zipAwareStorageCollectionItem.list());
    }

    @AfterClass
    public static void classTearDown() {
        TestUtil.cleanUpTestFiles();
    }
}
