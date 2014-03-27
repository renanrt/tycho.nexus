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

import org.codehaus.plexus.util.FileUtils;
import org.easymock.EasyMock;
import org.sonatype.nexus.mime.DefaultMimeSupport;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSPeer;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;

public class FSLocalRepositoryStorageMock extends DefaultFSLocalRepositoryStorage {

    private final File baseDir = FileUtils.createTempFile("nexus-unzip" + File.separator, "test-storage", null);

    public FSLocalRepositoryStorageMock(LinkPersister linkPersister) {
        // Wastebasket implementation cannot be retrieved with lookup()
        // -> use nice mock for now (deletion of items is not tested)
        super(EasyMock.createNiceMock(Wastebasket.class), linkPersister, new DefaultMimeSupport(), new DefaultFSPeer());
        if (baseDir.exists()) {
            try {
                FileUtils.forceDelete(baseDir);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public File getBaseDir(final Repository repository, final ResourceStoreRequest request) {
        return baseDir;
    }

}
