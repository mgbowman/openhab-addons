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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.util.UniFiSiteConfigDeserializer;

import com.google.gson.annotations.JsonAdapter;

/**
 * The {@link UniFiSiteConfigSection} represents a "named" section of a {@link UniFiSiteConfig};
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
@JsonAdapter(UniFiSiteConfigDeserializer.class)
public class UniFiSiteConfigSection {

    private final Map<String, Object> properties;

    public UniFiSiteConfigSection(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getKey() {
        return String.valueOf(properties.get("key"));
    }

    public @Nullable Boolean getAsBoolean(String key) {
        Object value = properties.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }

}
