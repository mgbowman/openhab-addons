/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link UniFiBindingConstants} class defines common constants, which are
 * used across the UniFi binding.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiBindingConstants {

    public static final String BINDING_ID = "unifi";

    // List of all Thing Types
    public final static ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public final static ThingTypeUID THING_TYPE_CLIENT = new ThingTypeUID(BINDING_ID, "client");

    // List of all Channels
    public final static String CHANNEL_ONLINE = "online";
    public final static String CHANNEL_AP = "ap";
    public final static String CHANNEL_RSSI = "rssi";

    // List of all Parameters
    public final static String PARAMETER_HOST = "host";
    public final static String PARAMETER_PORT = "port";
    public final static String PARAMETER_USERNAME = "username";
    public final static String PARAMETER_PASSWORD = "password";
    public final static String PARAMETER_SITE = "site";
    public final static String PARAMETER_MAC = "mac";

}
