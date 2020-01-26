/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.unifi.internal.handler.UniFiSiteThingHandler;

/**
 *
 * The {@link UniFiSiteThingConfig} encapsulates all the configuration options for an instance of the
 * {@link UniFiSiteThingHandler}.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiSiteThingConfig {

    private String sid = "";

    public String getSiteID() {
        return sid;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(sid);
    }

    @Override
    public String toString() {
        return String.format("UniFiSiteThingConfig{sid: '%s'}", sid);
    }

}
