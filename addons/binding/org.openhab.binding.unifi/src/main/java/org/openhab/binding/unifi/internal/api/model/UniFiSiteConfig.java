/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.unifi.internal.api.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link UniFiSiteConfig} represents a configuration composed of a 1..n {@link UniFiSiteConfigSection} instances.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiSiteConfig {

    protected static final String SECTION_MGMT = "mgmt";

    protected static final String OPTION_LED_ENABLED = "led_enabled";

    private final UniFiSite site;

    private final Map<String, UniFiSiteConfigSection> sections = new HashMap<>();

    public UniFiSiteConfig(UniFiSite site, UniFiSiteConfigSection[] sections) {
        this.site = site;
        for (UniFiSiteConfigSection section : sections) {
            String key = section.getKey();
            this.sections.put(key, section);
        }
    }

    public UniFiSite getSite() {
        return site;
    }

    public UniFiSiteConfigSection getSection(String key) {
        return sections.get(key);
    }

}
