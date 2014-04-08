/*******************************************************************************
 * Copyright (c) 2010, 2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.tycho.nexus.internal.plugin.storage;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.tycho.nexus.internal.plugin.DefaultUnzipRepository;
import org.eclipse.tycho.nexus.internal.plugin.cache.ConversionResult;
import org.eclipse.tycho.nexus.internal.plugin.cache.RequestPathConverter;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * This class implements a collection storage item that returns for contained zip files instances of
 * {@link ZipStorageCollectionItem} when its content is listed. By this a link to browse the zip
 * file is displayed when this collection is browsed in the web.
 */
public class ZipAwareStorageCollectionItem extends DefaultStorageCollectionItem {

    private final DefaultUnzipRepository repository;
    private final StorageCollectionItem collectionStorageItem;
    private final Logger logger;

    /**
     * Constructor.
     * 
     * @param repository
     *            the shadow repository
     * @param collectionStorageItem
     *            the file storage item that represents the zip file in the master repository
     */
    public ZipAwareStorageCollectionItem(final DefaultUnzipRepository repository,
            final StorageCollectionItem collectionStorageItem, final Logger logger) {
        super(repository, collectionStorageItem.getResourceStoreRequest(), true, false);
        this.repository = repository;
        this.collectionStorageItem = collectionStorageItem;
        this.logger = logger;
    }

    @Override
    public Collection<StorageItem> list() throws AccessDeniedException, NoSuchResourceStoreException,
            IllegalOperationException, ItemNotFoundException, LocalStorageException {
        final Collection<StorageItem> membersToDisplay = new LinkedList<StorageItem>();
        final ResourceStoreRequest request = new ResourceStoreRequest(collectionStorageItem.getPath()
                + "/artifact-1-SNAPSHOT.xml");
        final ConversionResult snapshotConversionResult = RequestPathConverter.convert(
                repository.getMasterRepository(), request, repository.isUseVirtualVersion());

        Collection<StorageItem> members;
        try {
            members = collectionStorageItem.list();
        } catch (@SuppressWarnings("deprecation") final org.sonatype.nexus.proxy.StorageException e) {
            throw new LocalStorageException(e);
        }
        for (final StorageItem member : members) {
            if (member instanceof StorageCollectionItem) {
                membersToDisplay.add(member);
            } else if (Util.checkIfZip(member)) {
                if (snapshotConversionResult.isPathConverted()) {
                    if (member.getPath().contains(snapshotConversionResult.getLatestVersion())) {
                        String virtualSnapshotArtifactPath = member.getPath().replace(
                                snapshotConversionResult.getLatestVersion(), "SNAPSHOT");
                        membersToDisplay.add(createStorageItemForRootOfZipFile(member, virtualSnapshotArtifactPath));
                    }
                } else {
                    membersToDisplay.add(createStorageItemForRootOfZipFile(member, member.getPath()));
                }
            }
        }

        return membersToDisplay;
    }

    private ZippedStorageCollectionItem createStorageItemForRootOfZipFile(final StorageItem member, String path)
            throws ItemNotFoundException, LocalStorageException {
        return new ZippedStorageCollectionItem(ZippedItem.newZippedChildItem(repository,
                collectionStorageItem.getItemContext(), path, "", member.getModified(), logger));
    }
}
