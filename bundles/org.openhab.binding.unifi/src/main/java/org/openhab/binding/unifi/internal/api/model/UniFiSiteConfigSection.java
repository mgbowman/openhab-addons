package org.openhab.binding.unifi.internal.api.model;

import java.util.Map;

import org.openhab.binding.unifi.internal.api.util.UniFiSiteConfigDeserializer;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(UniFiSiteConfigDeserializer.class)
public class UniFiSiteConfigSection {

    private final Map<String, Object> properties;

    public UniFiSiteConfigSection(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getKey() {
        return String.valueOf(properties.get("key"));
    }

    public Boolean getAsBoolean(String key) {
        Object value = properties.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }

}
