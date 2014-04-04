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
import java.util.Collection;

import org.eclipse.tycho.nexus.internal.plugin.DefaultUnzipRepository;
import org.eclipse.tycho.nexus.internal.plugin.test.TestUtil;
import org.eclipse.tycho.nexus.internal.plugin.test.UnzipPluginTestSupport;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;

@SuppressWarnings("nls")
public class ZippedStorageCollectionItemTest extends UnzipPluginTestSupport {
    private DefaultUnzipRepository unzipRepositoryMock;

    protected Logger testLogger = LoggerFactory.getLogger(getClass());

    @Before
    public void setupRepo() throws Exception {
        unzipRepositoryMock = createUnzipRepo(createMasterRepo());
    }

    @Test
    public void testList() throws ItemNotFoundException, IOException, AccessDeniedException,
            NoSuchResourceStoreException, IllegalOperationException {
        final ZippedItem zippedItem = createZippedItem("dir", 0L);
        final ZippedStorageCollectionItem zippedStorageCollectionItem = new ZippedStorageCollectionItem(zippedItem);
        TestUtil.assertMembers(new String[] { "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/dir/subdir" },
                new String[] { "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION + "/dir/test.txt" },
                zippedStorageCollectionItem.list());
    }

    @Test
    public void testListInRoot() throws ItemNotFoundException, IOException, AccessDeniedException,
            NoSuchResourceStoreException, IllegalOperationException {
        final ZippedItem zippedItem = createZippedItem("", 0L);
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

        final ZippedItem zipItem = createZippedItem("", time);
        final ZippedStorageCollectionItem zipStorageCollectionItem = new ZippedStorageCollectionItem(zipItem);
        Assert.assertEquals(time, zipStorageCollectionItem.getModified());

        //timestamps of zip entries are not time zone aware, therefore all entries shall inherit
        //the timestamp of the zip file itself
        final Collection<StorageItem> list = zipStorageCollectionItem.list();
        for (final StorageItem storageItem : list) {
            Assert.assertEquals(time, storageItem.getModified());
        }
    }

    private ZippedItem createZippedItem(String pathInZip, long lastModified) throws ItemNotFoundException,
            LocalStorageException {
        String pathToZip = "/dir/subdir/archive.zip";
        return ZippedItem.newZippedItem(unzipRepositoryMock,
                new ResourceStoreRequest(pathToZip + "-unzip/" + pathInZip), pathToZip, pathInZip, lastModified,
                testLogger);
    }

    @AfterClass
    public static void classTearDown() {
        TestUtil.cleanUpTestFiles();
    }
}
