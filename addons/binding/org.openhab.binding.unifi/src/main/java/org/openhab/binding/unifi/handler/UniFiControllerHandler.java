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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.unifi.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiController;
import org.openhab.binding.unifi.internal.UniFiControllerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiControllerHandler} is responsible for handling commands and status
 * updates for the UniFi Controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiControllerHandler extends BaseBridgeHandler implements Runnable {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(UniFiBindingConstants.THING_TYPE_CONTROLLER);

    private Logger logger = LoggerFactory.getLogger(UniFiControllerHandler.class);

    private UniFiControllerConfig config;

    private UniFiController controller;

    private ScheduledFuture<?> refreshJob = null;

    public UniFiControllerHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nop - read-only binding
        logger.info("Ignoring command = {} for channel = {} - the UniFi binding is read-only!", command, channelUID);
    }

    @Override
    public void initialize() {

        config = getConfig().as(UniFiControllerConfig.class);
        logger.debug("Initializing the UniFi Controller Handler with config = {}", config);

        if (!config.isValid()) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot validate UniFi Controller config. Please double-check your configuration.");
            return;
        }

        controller = new UniFiController(config);
        if (!controller.login()) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot login to the UniFi Controller. Please double-check the username and/or password and try again.");
            return;
        }

        if (controller.getDevices() == null) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot access the site named '" + config.getSite()
                            + "' on the UniFi Controller. Please double-check your site name and try again.");
            controller.logout();
            return;
        }

        controller.logout();

        scheduleRefreshJob();

        updateStatus(ONLINE);
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
    }

    private synchronized void scheduleRefreshJob() {
        logger.debug("Schedulling refresh job every {}s", config.getRefreshInterval().longValue());
        refreshJob = scheduler.scheduleAtFixedRate(this, 0, config.getRefreshInterval().longValue(), TimeUnit.SECONDS);
    }

    private synchronized void cancelRefreshJob() {
        if (refreshJob != null) {
            logger.debug("Cancelling refresh job");
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void run() {
        logger.debug("Polling the UniFi controller " + getThing().getUID());
        // UniFiController controller = new UniFiController(config);
        if (controller.login()) {
            if (getThing().getStatus() != ONLINE) {
                updateStatus(ONLINE);
            }
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler instanceof UniFiClientHandler) {
                    ((UniFiClientHandler) handler).refresh(controller);
                }
            }
            controller.logout();
        } else {
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not communicate with the UniFi controller");
        }
    }

    public UniFiController getController() {
        return controller;
    }

}
