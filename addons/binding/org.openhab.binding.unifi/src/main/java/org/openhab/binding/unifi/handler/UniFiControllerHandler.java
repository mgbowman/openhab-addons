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
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.unifi.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiController;
import org.openhab.binding.unifi.internal.UniFiControllerConfig;
import org.openhab.binding.unifi.internal.UniFiException;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiControllerHandler} is responsible for handling commands and status
 * updates for the UniFi Controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiControllerHandler extends BaseBridgeHandler implements Runnable {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(UniFiBindingConstants.THING_TYPE_CONTROLLER).collect(Collectors.toSet());

    private Logger logger = LoggerFactory.getLogger(UniFiControllerHandler.class);

    private UniFiControllerConfig config;

    private volatile UniFiController controller; /* mgb: volatile because accessed from multiple threads */

    private ScheduledFuture<?> refreshJob;

    public UniFiControllerHandler(Bridge bridge) {
        super(bridge);
    }

    protected UniFiController getController() {
        return controller;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nop - read-only binding
        logger.info("Ignoring command = {} for channel = {} - the UniFi binding is read-only!", command, channelUID);
    }

    @Override
    public void initialize() {
        Version version = bundleContext.getBundle().getVersion();
        logger.info("UniFi Binding v{}", version);

        // mgb: called when the config changes

        cancelRefreshJob();

        config = getConfig().as(UniFiControllerConfig.class);
        logger.debug("Initializing the UniFi Controller Handler with config = {}", config);

        try {
            controller = new UniFiController(config);
        } catch (UniFiException e) {
            logger.error("Error configuring the UniFi Controller: {}", e.getMessage());
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    e.getMessage() + " - Please double-check your configuration and try again.");
            controller = null;
            return;
        }

        scheduleRefreshJob();
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
    }

    private void scheduleRefreshJob() {
        logger.debug("Scheduling refresh job every {}s", config.getRefresh().longValue());
        refreshJob = scheduler.scheduleWithFixedDelay(this, 0, config.getRefresh().longValue(), TimeUnit.SECONDS);
    }

    private void cancelRefreshJob() {
        if (refreshJob != null) {
            logger.debug("Cancelling refresh job");
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    private void refresh() {
        ThingStatus status = getThing().getStatus();
        ThingStatusDetail statusDetail = getThing().getStatusInfo().getStatusDetail();
        String statusDescription = getThing().getStatusInfo().getDescription();

        if (controller == null) {
            // mgb: this should never happen
            logger.warn("UniFi Controller is NULL - this should never happen!");
        } else {
            // mgb: we're offline, we need to try to login
            if (status == INITIALIZING || (status == OFFLINE && statusDetail == COMMUNICATION_ERROR)) {
                logger.debug("Trying to establish communication with the UniFi controller");
                if (controller.login()) {
                    status = ONLINE;
                    statusDetail = ThingStatusDetail.NONE;
                    statusDescription = null;
                }
            }

            // mgb: only refresh if we're online
            if (status == ONLINE) {
                logger.debug("Refreshing the UniFi Controller {}", getThing().getUID());

                try {
                    // mgb: refresh the controller...
                    controller.refresh();

                    // mgb: ... then refresh all the clients
                    for (Thing thing : getThing().getThings()) {
                        ThingHandler handler = thing.getHandler();
                        if (handler instanceof UniFiClientHandler) {
                            UniFiClientHandler clientHandler = (UniFiClientHandler) handler;
                            clientHandler.refresh();
                        }
                    }
                } catch (UniFiException e) {
                    logger.warn("Error refreshing the UniFi Controller {} - {}", getThing().getUID(), e.getMessage());
                    status = OFFLINE;
                    statusDetail = COMMUNICATION_ERROR;
                    statusDescription = e.getMessage();
                }
            }
        }

        // mgb: update the status if it's changed
        if (status != getThing().getStatus()) {
            updateStatus(status, statusDetail, statusDescription);
        }
    }

    @Override
    public void run() {
        try {
            logger.trace("Executing refresh job");
            refresh();
        } catch (Exception e) {
            logger.error("Error executing refresh job: {}", e.getMessage(), e);
        }
    }

}
