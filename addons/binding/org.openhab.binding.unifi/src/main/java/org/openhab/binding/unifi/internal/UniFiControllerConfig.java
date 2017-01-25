/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

public class UniFiControllerConfig {

    private String host;
    private BigDecimal port;
    private String username;
    private String password;
    private String site;
    private BigDecimal refreshInterval;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public BigDecimal getPort() {
        return port;
    }

    public void setPort(BigDecimal port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public BigDecimal getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(BigDecimal refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    private String getUrl() {
        return "https://" + host + ":" + port + "/";
    }

    public String getLoginUrl() {
        return getUrl() + "api/login";
    }

    public String getClientsUrl() {
        return getUrl() + "api/s/" + site + "/stat/sta";
    }

    public String getDevicesUrl() {
        return getUrl() + "api/s/" + site + "/stat/device";
    }

    public String getLogoutUrl() {
        return getUrl() + "logout";
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(host) && port != null && StringUtils.isNotBlank(username)
                && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(site) && refreshInterval != null;
    }

    @Override
    public String toString() {
        return "UniFiControllerConfig{host = " + host + ", port = " + port + ", username = " + username
                + ", password = *****, site = " + site + ", refreshInterval = " + refreshInterval + "}";
    }
}