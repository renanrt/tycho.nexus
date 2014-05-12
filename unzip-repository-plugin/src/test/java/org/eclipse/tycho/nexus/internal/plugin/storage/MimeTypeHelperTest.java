/*******************************************************************************
 * Copyright (c) 2014 SAP AG and others.
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
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MimeTypeHelperTest {

    @Test
    public void testURLConnectionMimeTypes() throws Exception {
        assertEquals("text/html", MimeTypeHelper.guessMimeType("test/foo.html"));
    }

    @Test
    public void testAdditionalMimeTypes() throws Exception {
        assertEquals("text/css", MimeTypeHelper.guessMimeType("test/foo.css"));
        assertEquals("text/css", MimeTypeHelper.guessMimeType("test/foo.less"));
        assertEquals("application/javascript", MimeTypeHelper.guessMimeType("test/foo.js"));
        assertEquals("text/plain", MimeTypeHelper.guessMimeType("test/foo.properties"));
        assertEquals("application/json", MimeTypeHelper.guessMimeType("test/test.json"));
    }

    @Test
    public void testNoExtension() throws Exception {
        assertNull(MimeTypeHelper.guessMimeType("test/foo"));
    }

    @Test
    public void testMultipleDots() throws Exception {
        assertEquals("image/jpeg", MimeTypeHelper.guessMimeType("test/foo.bar.jpeg"));
    }

    @Test
    public void testUnknownExtension() throws Exception {
        assertNull(MimeTypeHelper.guessMimeType("test/foo.bar"));
    }

}
