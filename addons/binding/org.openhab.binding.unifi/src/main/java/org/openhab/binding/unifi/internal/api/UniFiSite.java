/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link UniFiSite} represents the data model of a UniFi site.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiSite {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("name")
    private String path;

    @JsonProperty("desc")
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean matchesName(String siteName) {
        return StringUtils.equalsIgnoreCase(name, siteName) || StringUtils.equals(path, siteName)
                || StringUtils.equalsIgnoreCase(id, siteName);
    }

    @Override
    public String toString() {
        return String.format("Site{name: '%s', path: '%s'}", name, path);
    }

}
