package org.openhab.binding.unifi.internal.api.model;

import java.util.HashMap;
import java.util.Map;

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
