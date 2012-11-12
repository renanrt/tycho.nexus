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

import java.io.IOException;

import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.eclipse.tycho.nexus.internal.plugin.DefaultUnzipRepository;
import org.eclipse.tycho.nexus.internal.plugin.storage.Util;
import org.eclipse.tycho.nexus.internal.plugin.storage.ZippedItem;
import org.eclipse.tycho.nexus.internal.plugin.test.RepositoryMock;
import org.eclipse.tycho.nexus.internal.plugin.test.TestUtil;
import org.eclipse.tycho.nexus.internal.plugin.test.UnzipRepositoryMock;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.util.ItemPathUtils;

@SuppressWarnings("nls")
public class ZippedItemTest {
    private final String pathToArchive = "/dir/subdir/archive.zip";
    private final String pathToUnzippedArchive = "/dir/subdir/archive.zip" + Util.UNZIP_TYPE_EXTENSION;
    private final DefaultUnzipRepository unzipReposMock = UnzipRepositoryMock.createUnzipRepository(RepositoryMock
            .createMasterRepo());

    @Test
    public void testZippedItemInRoot() throws ItemNotFoundException, IOException {
        final String pathInZip = "test.txt";
        final ZippedItem zippedItem = createZippedItem(pathInZip);

        Assert.assertEquals(pathInZip, zippedItem.getPathInZip());
        Assert.assertEquals(pathToUnzippedArchive + "/" + pathInZip, zippedItem.getPath());
        Assert.assertEquals("text/plain", zippedItem.getMimeType().toString());

        final DefaultStorageFileItem zippedStorageItem = (DefaultStorageFileItem) zippedItem.getZippedStorageItem();
        Assert.assertEquals(pathToUnzippedArchive + "/" + pathInZip, zippedStorageItem.getPath());
        Assert.assertEquals(pathToUnzippedArchive, zippedStorageItem.getParentPath());
        Assert.assertEquals("text/plain", zippedStorageItem.getMimeType().toString());
        TestUtil.assertContent("some content", zippedStorageItem);
    }

    @Test
    public void testZippedItemInDir() throws ItemNotFoundException, IOException {
        final String pathInZip = "dir/test.txt";
        final ZippedItem zippedItem = createZippedItem(pathInZip);

        Assert.assertEquals(pathInZip, zippedItem.getPathInZip());
        Assert.assertEquals(pathToUnzippedArchive + "/" + pathInZip, zippedItem.getPath());
        Assert.assertEquals("text/plain", zippedItem.getMimeType().toString());

        final DefaultStorageFileItem zippedStorageItem = (DefaultStorageFileItem) zippedItem.getZippedStorageItem();
        Assert.assertEquals(pathToUnzippedArchive + "/" + pathInZip, zippedStorageItem.getPath());
        Assert.assertEquals(pathToUnzippedArchive + "/dir", zippedStorageItem.getParentPath());
        Assert.assertEquals("text/plain", zippedStorageItem.getMimeType().toString());

        TestUtil.assertContent("some file content", zippedStorageItem);
    }

    @Test(expected = ItemNotFoundException.class)
    public void testZippedItemNotExisting() throws ItemNotFoundException, IOException {
        final String pathInZip = "x.txt";
        createZippedItem(pathInZip);
    }

    @Test(expected = ItemNotFoundException.class)
    public void testZippedItemNotExisting2() throws ItemNotFoundException, IOException {
        final String pathInZip = "dir/x.txt";
        createZippedItem(pathInZip);
    }

    @Test
    public void testZippedItemEmptyPath() throws ItemNotFoundException, IOException {
        final String pathInZip = "";
        final ZippedItem zippedItem = createZippedItem(pathInZip);

        Assert.assertEquals(pathInZip, zippedItem.getPathInZip());
        Assert.assertEquals(pathToUnzippedArchive, zippedItem.getPath());
        Assert.assertNull(zippedItem.getMimeType());

        final DefaultStorageCollectionItem zippedStorageItem = (DefaultStorageCollectionItem) zippedItem
                .getZippedStorageItem();
        Assert.assertEquals(pathToUnzippedArchive, zippedStorageItem.getPath());
    }

    @Test
    public void testZippedItemNullPath() throws ItemNotFoundException, IOException {
        final String pathInZip = null;
        final ZippedItem zippedItem = createZippedItem(pathInZip);

        Assert.assertEquals("", zippedItem.getPathInZip());
        Assert.assertEquals(pathToUnzippedArchive, zippedItem.getPath());
        Assert.assertNull(zippedItem.getMimeType());

        final DefaultStorageCollectionItem zippedStorageItem = (DefaultStorageCollectionItem) zippedItem
                .getZippedStorageItem();
        Assert.assertEquals(pathToUnzippedArchive, zippedStorageItem.getPath());
    }

    @Test
    public void testZippedItemDir() throws ItemNotFoundException, IOException {
        final String pathInZip = "dir";
        final ZippedItem zippedItem = createZippedItem(pathInZip);

        Assert.assertEquals(pathInZip, zippedItem.getPathInZip());
        Assert.assertEquals(pathToUnzippedArchive + "/" + pathInZip, zippedItem.getPath());
        Assert.assertNull(zippedItem.getMimeType());

        final DefaultStorageCollectionItem zippedStorageItem = (DefaultStorageCollectionItem) zippedItem
                .getZippedStorageItem();
        Assert.assertEquals(pathToUnzippedArchive + "/" + pathInZip, zippedStorageItem.getPath());
    }

    @Test
    public void testZippedItemDirWithTrailingSlash() throws ItemNotFoundException, IOException {
        final String pathInZip = "dir/";
        final ZippedItem zippedItem = createZippedItem(pathInZip);

        Assert.assertEquals(ItemPathUtils.cleanUpTrailingSlash(pathInZip), zippedItem.getPathInZip());
        Assert.assertEquals(ItemPathUtils.cleanUpTrailingSlash(pathToUnzippedArchive + "/" + pathInZip),
                zippedItem.getPath());
        Assert.assertNull(zippedItem.getMimeType());

        final DefaultStorageCollectionItem zippedStorageItem = (DefaultStorageCollectionItem) zippedItem
                .getZippedStorageItem();
        Assert.assertEquals(ItemPathUtils.cleanUpTrailingSlash(pathToUnzippedArchive + "/" + pathInZip),
                zippedStorageItem.getPath());
    }

    @Test(expected = LocalStorageException.class)
    public void testZipItemNotFound() throws Exception {
        final String pathInZip = "test.txt";
        new ZippedItem(unzipReposMock, "/", pathInZip, 0L, new ConsoleLogger());
    }

    @Test
    public void testListMembersEmptyPath() throws ItemNotFoundException, IOException {
        final String pathInZip = "";
        final ZippedItem zippedItem = createZippedItem(pathInZip);

        TestUtil.assertMembers(new String[] { pathToUnzippedArchive + "/dir" }, new String[] { pathToUnzippedArchive
                + "/test.txt" }, zippedItem.listMembers());
    }

    @Test
    public void testListMembersDir() throws ItemNotFoundException, IOException {
        final String pathInZip = "dir/";
        final ZippedItem zippedItem = createZippedItem(pathInZip);

        TestUtil.assertMembers(new String[] { pathToUnzippedArchive + "/" + pathInZip + "subdir" },
                new String[] { pathToUnzippedArchive + "/" + pathInZip + "test.txt" }, zippedItem.listMembers());
    }

    @Test(expected = LocalStorageException.class)
    public void testListMembersOnFile() throws ItemNotFoundException, IOException {
        final String pathInZip = "test.txt";
        final ZippedItem zippedItem = createZippedItem(pathInZip);
        zippedItem.listMembers();
    }

    private ZippedItem createZippedItem(final String pathInZip) throws LocalStorageException, ItemNotFoundException {
        return new ZippedItem(unzipReposMock, pathToArchive, pathInZip, System.currentTimeMillis(), new ConsoleLogger());
    }

    @AfterClass
    public static void classTearDown() {
        TestUtil.cleanUpTestFiles();
    }
}
