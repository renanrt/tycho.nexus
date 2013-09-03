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

import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

public class Util {

    public static final String UNZIP_TYPE_EXTENSION = "-unzip";

    /**
     * Checks whether the given item represents a zip file.
     * 
     * @param item
     *            the item for which it should be checked whether it represents a zip file
     * @return <code>true</code> if the given item represents a zip file, otherwise
     *         <code>false</code>
     */
    public static boolean checkIfZip(final StorageItem item) {
        if (item instanceof StorageFileItem) {
            final String mimeType = ((StorageFileItem) item).getContentLocator().getMimeType();
            return "application/zip".equals(mimeType) || "application/java-archive".equals(mimeType);
        }
        return false;
    }
}
