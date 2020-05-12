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

import java.time.ZonedDateTime;

import org.apache.commons.lang.BooleanUtils;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.util.UniFiTidyLowerCaseStringDeserializer;
import org.openhab.binding.unifi.internal.api.util.UniFiTimestampDeserializer;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link UniFiClient} is the base data model for any (wired or wireless) connected to a UniFi network.
 *
 * @author Matthew Bowman - Initial contribution
 * @author Patrik Wimnell - Blocking / Unblocking client support
 */
public abstract class UniFiClient {

    protected final transient UniFiController controller;

    @SerializedName("_id")
    protected String id;

    protected String siteId;

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    protected String mac;

    protected String ip;

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    protected String hostname;

    @SerializedName("name")
    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    protected String alias;

    protected Integer uptime;

    @JsonAdapter(UniFiTimestampDeserializer.class)
    protected ZonedDateTime lastSeen;

    protected boolean blocked;

    @SerializedName("is_guest")
    protected boolean guest;

    @SerializedName("satisfaction")
    protected Integer experience;

    protected UniFiClient(UniFiController controller) {
        this.controller = controller;
    }

    public String getId() {
        return id;
    }

    public String getMac() {
        return mac;
    }

    public String getIp() {
        return this.ip;
    }

    public String getHostname() {
        return hostname;
    }

    public String getAlias() {
        return alias;
    }

    public Integer getUptime() {
        return uptime;
    }

    public ZonedDateTime getLastSeen() {
        return lastSeen;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public abstract Boolean isWired();

    public final Boolean isWireless() {
        return BooleanUtils.negate(isWired());
    }

    public boolean isGuest() {
        return guest;
    }

    protected abstract String getDeviceMac();

    public UniFiSite getSite() {
        return controller.getSite(siteId);
    }

    public UniFiDevice getDevice() {
        return controller.getDevice(getDeviceMac());
    }

    public Integer getExperience() {
        return experience;
    }

    // Functional API

    public void block(boolean blocked) throws UniFiException {
        UniFiControllerRequest<Void> req = controller.newRequest(Void.class);
        req.setPath("/api/s/" + getSite().getName() + "/cmd/stamgr");
        req.setBodyParameter("cmd", blocked ? "block-sta" : "unblock-sta");
        req.setBodyParameter("mac", mac);
        controller.executeRequest(req);
    }

    public void reconnect() throws UniFiException {
        UniFiControllerRequest<Void> req = controller.newRequest(Void.class);
        req.setPath("/api/s/" + getSite().getName() + "/cmd/stamgr");
        req.setBodyParameter("cmd", "kick-sta");
        req.setBodyParameter("mac", mac);
        controller.executeRequest(req);
    }

    @Override
    public String toString() {
        return String.format(
                "UniFiClient{mac: '%s', ip: '%s', hostname: '%s', alias: '%s', wired: %b, device: %s, guest: %s, blocked: %b, experience: %d}",
                mac, ip, hostname, alias, isWired(), getDevice(), guest, blocked, experience);
    }
}
