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
import org.openhab.binding.unifi.internal.api.json.adapters.UniFiTidyLowerCaseStringDeserializer;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link UniFiClient} is the base data model for any (wired or wireless) connected to a UniFi network.
 *
 * @author Matthew Bowman - Initial contribution
 */
public abstract class UniFiClient {

    @SerializedName("_id")
    protected String id;

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    protected String mac;

    protected String hostname;

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
