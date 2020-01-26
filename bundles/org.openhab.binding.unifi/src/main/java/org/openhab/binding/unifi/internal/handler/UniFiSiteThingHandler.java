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
package org.openhab.binding.unifi.internal.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.unifi.internal.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiSiteThingConfig;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.model.UniFiController;
import org.openhab.binding.unifi.internal.api.model.UniFiSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiSiteThingHandler} is responsible for handling commands and status
 * updates for {@link UniFiSite} instances.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiSiteThingHandler extends UniFiBaseThingHandler<UniFiSite, UniFiSiteThingConfig> {

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return UniFiBindingConstants.THING_TYPE_SITE.equals(thingTypeUID);
    }

    private final Logger logger = LoggerFactory.getLogger(UniFiSiteThingHandler.class);

    private UniFiSiteThingConfig config = new UniFiSiteThingConfig();

    public UniFiSiteThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected synchronized void initialize(UniFiSiteThingConfig config) {
        if (thing.getStatus() == INITIALIZING) {
            if (!config.isValid()) {
                updateStatus(OFFLINE, CONFIGURATION_ERROR);
                return;
            }
            this.config = config;
            updateStatus(ONLINE);
        }
    }

    @Override
    protected synchronized @Nullable UniFiSite getEntity(UniFiController controller) {
        return controller.getSite(config.getSiteID());
    }

    @Override
    protected void refreshChannel(UniFiSite site, ChannelUID channelUID) {
        String channelID = channelUID.getIdWithoutGroup();
        State state = new DecimalType(0);
        switch (channelID) {
            case CHANNEL_TOTAL_CLIENTS:
                state = new DecimalType(site.getTotalClientCount());
                break;
            case CHANNEL_WIRELESS_CLIENTS:
                state = new DecimalType(site.getWirelessClientCount());
                break;
            case CHANNEL_WIRED_CLIENTS:
                state = new DecimalType(site.getWiredClientCount());
                break;
            case CHANNEL_GUEST_CLIENTS:
                state = new DecimalType(site.getGuestClientCount());
                break;
            case CHANNEL_LED:
                state = OnOffType.from(site.isLedEnabled());
                break;
        }
        updateState(channelUID, state);
    }

    @Override
    protected void handleCommand(UniFiSite site, ChannelUID channelUID, Command command) throws UniFiException {
        String channelID = channelUID.getIdWithoutGroup();
        switch (channelID) {
            case CHANNEL_LED:
                handleLedCommand(site, channelUID, command);
                break;
            default:
                logger.warn("Ignoring unsupported command = {} for channel = {}", command, channelUID);
        }
    }

    private void handleLedCommand(UniFiSite site, ChannelUID channelUID, Command command) throws UniFiException {
        if (command instanceof OnOffType) {
            site.setLedEnabled(command == OnOffType.ON);
        } else {
            logger.warn("Ignoring unsupported command = {} for channel = {} - valid commands types are: OnOffType",
                    command, channelUID);
        }
    }

}
