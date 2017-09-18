/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

/**
 *
 * The {@link UniFiControllerConfig} encapsulates all the configuration options for an instance of the
 * {@link UniFiController}.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiControllerConfig {

    private String host;
    private BigDecimal port;
    private String username;
    private String password;
    private BigDecimal refresh;
    private BigDecimal considerHome;

    public String getHost() {
        return host;
    }

    public BigDecimal getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public BigDecimal getRefresh() {
        return refresh;
    }

    public BigDecimal getConsiderHome() {
        return considerHome;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(host) && port != null && StringUtils.isNotBlank(username)
                && StringUtils.isNotBlank(password) && refresh != null && considerHome != null;
    }

    @Override
    public String toString() {
        return "UniFiControllerConfig{host = " + host + ", port = " + port + ", username = " + username
                + ", password = *****, refresh = " + refresh + ", considerHome = " + considerHome + "}";
    }
}
