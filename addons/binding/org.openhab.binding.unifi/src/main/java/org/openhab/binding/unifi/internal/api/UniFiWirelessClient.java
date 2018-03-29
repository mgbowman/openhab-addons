/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api;

import java.util.Calendar;

import org.openhab.binding.unifi.internal.api.json.UniFiMacDeserializer;
import org.openhab.binding.unifi.internal.api.json.UniFiTimestampDeserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A {@link UniFiWirelessClient} represents a wireless {@link UniFiClient}.
 *
 * A wireless client is not physically connected to the network - typically it is connected via a Wi-Fi adapter.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiWirelessClient extends UniFiClient {

    @JsonProperty("ap_mac")
    @JsonDeserialize(using = UniFiMacDeserializer.class)
    private String apMac;

    private String essid;

    private Integer rssi;

    private Integer uptime;

    @JsonProperty("last_seen")
    @JsonDeserialize(using = UniFiTimestampDeserializer.class)
    private Calendar lastSeen;

    public Integer getRssi() {
        return rssi;
    }

    public String getEssid() {
        return essid;
    }

    @Override
    public String getDeviceMac() {
        return apMac;
    }

    public Calendar getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Calendar lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Integer getUptime() {
        return uptime;
    }

    public void setUptime(Integer uptime) {
        this.uptime = uptime;
    }

    @Override
    public Boolean isWired() {
        return false;
    }

}
