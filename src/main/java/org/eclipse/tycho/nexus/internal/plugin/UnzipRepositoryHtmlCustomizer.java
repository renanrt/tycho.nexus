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

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Component(role = NexusIndexHtmlCustomizer.class, hint = "UnzipRepositoryHtmlCustomizer")
public class UnzipRepositoryHtmlCustomizer extends AbstractNexusIndexHtmlCustomizer {
    /*
     * Normally this script tag should be inserted into the head of the page using
     * getPostHeadContribution, BUT as with the current nexus plugin system it is possible that
     * other plug-ins overwrite the already overwritten JS function with code only fitting to their
     * usage type, e.g. the OBR plug-in of Nexus Pro enhances the Virtual Repository Editor.
     * 
     * By writing it in the first part of the Body we ensure for now, that our script is loaded as
     * last one. Works only as long as we are the only one doing this so. The script provided with
     * this ensures that other repository type can still select all available repositories without
     * filtering.
     * 
     * Overall this can't be the final solution, possibly it is neccessary to update the way users
     * enhance the Nexus UI in general. In particular useful would be if you can define filtering of
     * choosable repositories for a specific repository type already in the nexus repository model.
     */
    @Override
    public String getPreBodyContribution(final java.util.Map<String, Object> context) {
        final String version = getVersionFromJarFile("/META-INF/maven/org.eclipse.tycho.nexus/unzip-repository-plugin/pom.properties");

        return "<script src=\"" + UnzipRepositoryResourceBundle.JS_SCRIPT_PATH + (version == null ? "" : "?" + version)
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
    }
}
