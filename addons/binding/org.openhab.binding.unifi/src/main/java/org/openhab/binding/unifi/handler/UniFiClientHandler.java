/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.BRIDGE_OFFLINE;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.unifi.UniFiBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.unifi.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiClientConfig;
import org.openhab.binding.unifi.internal.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiDevice;
import org.openhab.binding.unifi.internal.api.UniFiSite;
import org.openhab.binding.unifi.internal.api.UniFiWirelessClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiClientHandler} is responsible for handling commands and status
 * updates for UniFi Wireless Devices.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiClientHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(UniFiBindingConstants.THING_TYPE_CLIENT).collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(UniFiClientHandler.class);

    private volatile UniFiClientConfig config; /* mgb: volatile because accessed from multiple threads */

    public UniFiClientHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // mgb: called when the config changes

        config = getConfig().as(UniFiClientConfig.class).tidy();
        logger.debug("Initializing the UniFi Client Handler with config = {}", config);

        if (!config.isValid()) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid MAC address.");
            return;
        }

        Bridge bridge = getBridge();

        if (bridge == null || bridge.getHandler() == null || !(bridge.getHandler() instanceof UniFiControllerHandler)) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "You must choose a UniFi Controller for this UniFi Wireless Device.");
            return;
        }

        if (bridge.getStatus() == OFFLINE) {
            updateStatus(OFFLINE, BRIDGE_OFFLINE, "The UniFi Controller is currently offline.");
            return;
        }

        // mgb: only refreshes if we we're ONLINE
        refresh();

        updateStatus(ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command = {} for channel = {}", command, channelUID);
        if (command == REFRESH) {
            refresh(channelUID);
        } else {
            logger.debug("Ignoring unsupported command = {} for channel = {} - the UniFi binding is read-only!",
                    command, channelUID);
        }
    }

    public void refresh() {
        // mgb: only refresh if we're ONLINE
        if (getThing().getStatus() == ONLINE) {
            for (Channel channel : getThing().getChannels()) {
                ChannelUID channelUID = channel.getUID();
                refresh(channelUID);
            }
        }
    }

    private State getDefaultState(String channelID, boolean clientOnline) {
        State state = null;
        switch (channelID) {
            case CHANNEL_ONLINE:
            case CHANNEL_SITE:
            case CHANNEL_AP:
            case CHANNEL_ESSID:
            case CHANNEL_RSSI:
                state = (clientOnline ? null : UnDefType.UNDEF); // skip the update if the client is online
                break;
            case CHANNEL_UPTIME:
                // mgb: uptime should default to 0 seconds
                state = (clientOnline ? null : new DecimalType(0)); // skip the update if the client is online
                break;
            case CHANNEL_LAST_SEEN:
                // mgb: lastSeen should keep the last state no matter what
                state = null;
                break;
        }
        return state;
    }

    private void refresh(ChannelUID channelUID) {
        // mgb: only refresh if we're ONLINE
        if (getThing().getStatus() == ONLINE) {
            logger.debug("Refreshing channel = {}", channelUID);

            UniFiController controller = ((UniFiControllerHandler) getBridge().getHandler()).getController();
            UniFiWirelessClient client = controller.getWirelessClient(config.getMac(), config.getSite());
            if (client == null) {
                logger.debug("Could not find a matching client: mac = {}, site = {}", config.getMac(),
                        config.getSite());
            }
            boolean clientOnline = controller.isWirelessClientOnline(client);
            UniFiDevice device = (client == null ? null : client.getDevice());
            UniFiSite site = (device == null ? null : device.getSite());

            String channelID = channelUID.getIdWithoutGroup();
            State state = getDefaultState(channelID, clientOnline);

            switch (channelID) {
                // :online
                case CHANNEL_ONLINE:
                    state = (clientOnline ? OnOffType.ON : OnOffType.OFF);
                    break;

                // :site
                case CHANNEL_SITE:
                    if (clientOnline && site != null && StringUtils.isNotBlank(site.getName())) {
                        state = StringType.valueOf(site.getName());
                    }
                    break;

                // :ap
                case CHANNEL_AP:
                    if (clientOnline && device != null && StringUtils.isNotBlank(device.getName())) {
                        state = StringType.valueOf(device.getName());
                    }
                    break;

                // :essid
                case CHANNEL_ESSID:
                    if (clientOnline && client != null && StringUtils.isNotBlank(client.getEssid())) {
                        state = StringType.valueOf(client.getEssid());
                    }
                    break;

                // :rssi
                case CHANNEL_RSSI:
                    if (clientOnline && client != null && client.getRssi() != null) {
                        state = new DecimalType(client.getRssi());
                    }
                    break;

                // :uptime
                case CHANNEL_UPTIME:
                    if (clientOnline && client != null && client.getUptime() != null) {
                        state = new DecimalType(client.getUptime());
                    }
                    break;

                // :lastSeen
                case CHANNEL_LAST_SEEN:
                    // mgb: we don't check clientOnline as lastSeen is also included in the Insights data
                    if (/* clientOnline && */ client != null && client.getLastSeen() != null) {
                        state = new DateTimeType(client.getLastSeen());
                    }
                    break;
            }

            // mgb: only non null states get updates
            if (state != null) {
                updateState(channelID, state);
            }
        }
    }

}
