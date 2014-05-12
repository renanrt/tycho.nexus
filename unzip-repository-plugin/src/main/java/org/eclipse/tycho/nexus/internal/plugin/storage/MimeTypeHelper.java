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

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class MimeTypeHelper {

    private static final Map<String, String> EXTENSION_TO_MIMETYPE_MAP = createExtensionToMimetypeMap();

    private static Map<String, String> createExtensionToMimetypeMap() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("js", "application/javascript");
        result.put("properties", "text/plain");
        result.put("json", "application/json");
        result.put("css", "text/css");
        result.put("less", "text/css");
        return result;
    }

    private MimeTypeHelper() {
    }

    public static String guessMimeType(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        if (mimeType == null) {
            int lastDot = path.lastIndexOf('.');
            if (lastDot != -1) {
                mimeType = EXTENSION_TO_MIMETYPE_MAP.get(path.substring(lastDot + 1));
            }
        }
        return mimeType;
    }
}
