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

import org.eclipse.tycho.nexus.internal.plugin.test.RepositoryMock;

/**
 * Tests the DefaultUnzipRepository with a proxy repository as master repository.
 * 
 * With proxy repositories there is one problem in Nexus. If a proxy repository is asked for a
 * collection that exists in the proxied repository and no artifact under this collection (directly
 * under it or deeper in the hierarchy) has been cached in the proxy repository the request fails
 * with ItemNotFoundException.
 * 
 * Since this behavior is different from a hosted repository and since it has influence on the
 * functionality of the unzip repository we have here an explicit test against a proxy repository
 * that for test purposes always throws an ItemNotFoundException when a collection is accessed.
 * 
 * Because of this this test does not include any test cases that directly request a collection (in
 * opposite to {@link DefaultUnzipRepositoryAgainstHostedRepositoryTest}).
 */
public class DefaultUnzipRepositoryAgainstProxyRepositoryTest extends DefaultUnzipRepositoryTest {

    @Override
    protected RepositoryMock createRepositoryMock() throws Exception {
        final RepositoryMock masterRepo = createMasterRepo();
        masterRepo.setBehaveAsProxy(true);
        return masterRepo;
    }
}
