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

import java.util.Arrays;
import java.util.Collection;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * This class implements a collection storage item for the root folder or a sub-folder inside a zip
 * file that allows to list the direct members of the folder.
 */
public class ZippedStorageCollectionItem extends DefaultStorageCollectionItem {

    private final ZippedItem zippedItem;

    /**
     * Constructor.
     * 
     * @param zippedItem
     *            a zipped item that represents a folder in a zip file
     * @param modified
     *            the modification time of the represented file
     */
    public ZippedStorageCollectionItem(final ZippedItem zippedItem) {
        super(zippedItem.getRepository(), zippedItem.getRequest(), true, false);
        this.zippedItem = zippedItem;
        setModified(zippedItem.getLastModified());
    }

    @Override
    public Collection<StorageItem> list() throws AccessDeniedException, NoSuchResourceStoreException,
            IllegalOperationException, ItemNotFoundException, LocalStorageException {
        return Arrays.asList(zippedItem.listMembers());
    }
}
