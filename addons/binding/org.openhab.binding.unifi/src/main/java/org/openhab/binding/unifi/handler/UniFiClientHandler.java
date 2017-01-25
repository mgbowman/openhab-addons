/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.openhab.binding.unifi.UniFiBindingConstants.CHANNEL_ONLINE;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.unifi.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiClientConfig;
import org.openhab.binding.unifi.internal.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiClientHandler} is responsible for handling commands and status
 * updates for UniFi Wireless Devices.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiClientHandler extends BaseThingHandler {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(UniFiBindingConstants.THING_TYPE_CLIENT);

    private Logger logger = LoggerFactory.getLogger(UniFiClientHandler.class);

    private UniFiClientConfig config;

    public UniFiClientHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        // nop - read-only binding
        logger.info("Ignoring command = {} for channel = {} - the UniFi binding is read-only!", command, channelUID);

        if (channelUID.getId().equals(CHANNEL_ONLINE)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }

    }

    @Override
    public void initialize() {

        Bridge bridge = getBridge();
        config = getConfig().as(UniFiClientConfig.class);

        logger.debug("Initializing the UniFi Client Handler with bridge = {}, config = {}", bridge, config);

        if (bridge == null) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "You must choose a UniFi Controller for this UniFi Wireless Device");
        } else {

            // TODO add mac regex

            updateStatus(ONLINE);

            // updateState(CHANNEL_ONLINE, UnDefType.UNDEF);
            // updateState(CHANNEL_AP, UnDefType.UNDEF);
            // updateState(CHANNEL_RSSI, UnDefType.UNDEF);

        }

    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == OFFLINE
                && bridgeStatusInfo.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR) {
            updateState(CHANNEL_ONLINE, UnDefType.UNDEF);
            // updateState(CHANNEL_AP, UnDefType.UNDEF);
            // updateState(CHANNEL_RSSI, UnDefType.UNDEF);
        }
    }

    public void refresh(UniFiController controller) {
        logger.debug("Refreshing UniFi device {}", getThing().getUID());
        UniFiClient client = controller.getClient(config.getMac());
        updateState(CHANNEL_ONLINE, client == null ? OnOffType.OFF : OnOffType.ON);
        if (client != null) {
            // updateState(CHANNEL_AP, null);
            // updateState(CHANNEL_RSSI, null);
        }
    }

}
