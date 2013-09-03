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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.tycho.nexus.internal.plugin.DefaultUnzipRepository;
import org.eclipse.tycho.nexus.internal.plugin.test.RepositoryMock;
import org.eclipse.tycho.nexus.internal.plugin.test.TestUtil;
import org.eclipse.tycho.nexus.internal.plugin.test.UnzipRepositoryMock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.item.StorageItem;

@SuppressWarnings("nls")
public class ZippedStorageCollectionItemTest {
    private DefaultUnzipRepository unzipRepositoryMock;

    protected Logger testLogger = LoggerFactory.getLogger(getClass());

    @Before
    public void setupRepo() {
        unzipRepositoryMock = UnzipRepositoryMock.createUnzipRepository(RepositoryMock.createMasterRepo());
    }

    @Test
    public void testList() throws ItemNotFoundException, IOException, AccessDeniedException,
            NoSuchResourceStoreException, IllegalOperationException {
        final ZippedItem zippedItem =
            new ZippedItem(unzipRepositoryMock, "/dir/subdir/archive.zip", "dir", 0L, testLogger);
        final ZippedStorageCollectionItem zippedStorageCollectionItem = new ZippedStorageCollectionItem(zippedItem);
        TestUtil.assertMembers(new String[] { "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/dir/subdir" },
            new String[] { "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/dir/test.txt" },
            zippedStorageCollectionItem.list());
    }

    @Test
    public void testListInRoot() throws ItemNotFoundException, IOException, AccessDeniedException,
            NoSuchResourceStoreException, IllegalOperationException {
        final ZippedItem zippedItem =
            new ZippedItem(unzipRepositoryMock, "/dir/subdir/archive.zip", "", 0L, testLogger);
        final ZippedStorageCollectionItem zippedStorageCollectionItem = new ZippedStorageCollectionItem(zippedItem);
        TestUtil.assertMembers(new String[] { "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/dir" },
            new String[] { "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/test.txt" },
            zippedStorageCollectionItem.list());
    }

    @Test
    public void testModificationTimeInRoot() throws ItemNotFoundException, IOException, AccessDeniedException,
            NoSuchResourceStoreException, IllegalOperationException {
        final File file = new File("./src/test/resources/" + "masterRepo" + "/dir/subdir/archive.zip");
        final long time = file.lastModified();

        final ZippedItem zipItem = new ZippedItem(unzipRepositoryMock, "/dir/subdir/archive.zip", "", time, testLogger);
        final ZippedStorageCollectionItem zipStorageCollectionItem = new ZippedStorageCollectionItem(zipItem);
        assertEquals(time, zipStorageCollectionItem.getModified());

        //timestamps of zip entries are not time zone aware, therefore all entries shall inherit
        //the timestamp of the zip file itself
        final Collection<StorageItem> list = zipStorageCollectionItem.list();
        for (final StorageItem storageItem : list) {
            assertEquals(time, storageItem.getModified());
        }
    }

    @AfterClass
    public static void classTearDown() {
        TestUtil.cleanUpTestFiles();
    }
}
