/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api;

import org.apache.commons.lang.BooleanUtils;
import org.openhab.binding.unifi.internal.api.json.UniFiMacDeserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * The {@link UniFiClient} is the base data model for any (wired or wireless) connected to a UniFi network.
 *
 * @author Matthew Bowman - Initial contribution
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "is_wired", defaultImpl = UniFiUnknownClient.class)
@JsonSubTypes({ @JsonSubTypes.Type(value = UniFiWiredClient.class, name = "true"),
        @JsonSubTypes.Type(value = UniFiWirelessClient.class, name = "false") })
public abstract class UniFiClient {

    @JsonProperty("_id")
    protected String id;

    @JsonDeserialize(using = UniFiMacDeserializer.class)
    protected String mac;

    protected String hostname;

    @JsonProperty("site_id")
    protected String siteId;

    protected UniFiDevice device;

    public String getId() {
        return id;
    }

    public UniFiDevice getDevice() {
        return device;
    }

    public void setDevice(UniFiDevice ap) {
        this.device = ap;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public abstract Boolean isWired();

    public final Boolean isWireless() {
        return BooleanUtils.negate(isWired());
    }

    public abstract String getDeviceMac();

    @Override
    public String toString() {
        return String.format("UniFiClient{mac: '%s', hostname: '%s', wired: %b, device: %s}", mac, hostname, isWired(),
                device);
    }

}
