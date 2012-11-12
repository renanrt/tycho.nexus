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
package org.eclipse.tycho.nexus.internal.plugin.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

@SuppressWarnings("nls")
@Ignore
public class TestUtil {

    public static void assertContent(final String expectedContent, final DefaultStorageFileItem item)
            throws IOException {
        Assert.assertNotNull(item);
        final byte[] content = new byte[expectedContent.length()];
        final int len = item.getInputStream().read(content);
        Assert.assertEquals(expectedContent, new String(content, 0, len));
    }

    public static void assertMembers(final String[] expectedCollections, final String[] expectedFiles,
            final Collection<StorageItem> actualMembers) {
        final List<String> expectedCollectionsList = new ArrayList<String>(Arrays.asList(expectedCollections));
        final List<String> expectedFilesList = new ArrayList<String>(Arrays.asList(expectedFiles));
        Assert.assertEquals(expectedCollections.length + expectedFiles.length, actualMembers.size());
        final Iterator<StorageItem> actualMemberIt = actualMembers.iterator();
        while (actualMemberIt.hasNext()) {
            final StorageItem actualMember = actualMemberIt.next();
            if (actualMember instanceof StorageCollectionItem) {
                Assert.assertTrue("unexpected collection member [" + actualMember.getPath() + "]",
                        expectedCollectionsList.remove(actualMember.getPath()));
            } else if (actualMember instanceof StorageFileItem) {
                Assert.assertTrue("unexpected file member [" + actualMember.getPath() + "]",
                        expectedFilesList.remove(actualMember.getPath()));
            } else {
                Assert.fail("unexpected storage item [" + actualMember.getPath() + "]");
            }
        }
        Assert.assertTrue(expectedCollectionsList.isEmpty());
        Assert.assertTrue(expectedFilesList.isEmpty());
    }

    public static void assertMembers(final String[] expectedCollections, final String[] expectedFiles,
            final StorageItem[] actualMembers) {
        assertMembers(expectedCollections, expectedFiles, Arrays.asList(actualMembers));
    }

    public static StorageItem findItem(final String path, final Collection<StorageItem> items) {
        for (final StorageItem item : items) {
            if (path.equals(item.getPath())) {
                return item;
            }
        }
        return null;
    }

    public static void cleanUpTestFiles() {
        try {
            FileUtils.deleteDirectory(FileUtils.createTempFile("nexus-unzip" + File.separator, "", null)
                    .getParentFile());
        } catch (final IOException e) {
            System.out.println("Warning: Test clean up: " + e.getMessage());
        }
    }
}
