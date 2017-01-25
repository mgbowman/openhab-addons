/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link UniFiClient} represents the data model of a UniFi Wireless Client
 * (like a Laptop, Phone or Tablet).
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiClient {

    private String mac;

    private String hostname;

    private Integer rssi;

    private String essid;

    private UniFiDevice ap;

    @JsonProperty("ap_mac")
    private String apMac;

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public String getEssid() {
        return essid;
    }

    public void setEssid(String essid) {
        this.essid = essid;
    }

    public UniFiDevice getAp() {
        return ap;
    }

    public void setAp(UniFiDevice ap) {
        this.ap = ap;
    }

    public String getApMac() {
        return apMac;
    }

    public void setApMac(String apMac) {
        this.apMac = apMac;
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

}
