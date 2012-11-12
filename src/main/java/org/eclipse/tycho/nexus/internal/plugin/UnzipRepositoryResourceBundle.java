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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

@Component(role = NexusResourceBundle.class, hint = "UnzipRepositoryResourceBundle")
public class UnzipRepositoryResourceBundle extends AbstractNexusResourceBundle {
    public static final String JS_SCRIPT_PATH = "js/unzip/unzip-repo.js";

    @Override
    public List<StaticResource> getContributedResouces() {
        final List<StaticResource> result = new ArrayList<StaticResource>();

        result.add(new DefaultStaticResource(getClass().getResource("/resources/js/unzip-repo.js"), "/"
                + JS_SCRIPT_PATH, "application/x-javascript"));

        return result;
    }
}
