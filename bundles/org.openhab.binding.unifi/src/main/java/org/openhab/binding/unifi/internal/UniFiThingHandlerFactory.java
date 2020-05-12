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
package org.openhab.binding.unifi.internal;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.HttpClientInitializationException;
import org.openhab.binding.unifi.internal.handler.UniFiClientThingHandler;
import org.openhab.binding.unifi.internal.handler.UniFiControllerThingHandler;
import org.openhab.binding.unifi.internal.handler.UniFiSiteThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthew Bowman - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.unifi")
public class UniFiThingHandlerFactory extends BaseThingHandlerFactory {

    // public static final UniFiTrustManagerProvider DEFAULT_TRUST_MANAGER_PROVIDER = new UniFiTrustManagerProvider(
    // "UniFi");

    private final Logger logger = LoggerFactory.getLogger(UniFiThingHandlerFactory.class);

    private HttpClientFactory httpClientFactory;

    // private ExtensibleTrustManager extensibleTrustManager;

    private HttpClient httpClient;

    public UniFiThingHandlerFactory() {
        // mgb: reverting to in-binding httpClient until ssl connection issues are solved
        httpClient = new HttpClient(new SslContextFactory(true));
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new HttpClientInitializationException("Could not start HttpClient", e);
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return UniFiControllerThingHandler.supportsThingType(thingTypeUID)
                || UniFiClientThingHandler.supportsThingType(thingTypeUID)
                || UniFiSiteThingHandler.supportsThingType(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (UniFiControllerThingHandler.supportsThingType(thingTypeUID)) {
            // return new UniFiControllerThingHandler((Bridge) thing, httpClient, extensibleTrustManager);
            return new UniFiControllerThingHandler((Bridge) thing, httpClient);
        } else if (UniFiClientThingHandler.supportsThingType(thingTypeUID)) {
            return new UniFiClientThingHandler(thing);
        } else if (UniFiSiteThingHandler.supportsThingType(thingTypeUID)) {
            return new UniFiSiteThingHandler(thing);
        }
        return null;
    }

    @Reference
    public void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    public void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        if (this.httpClientFactory == httpClientFactory) {
            this.httpClientFactory = null;
        }
    }

    // @Reference
    // public void setExtensibleTrustManager(ExtensibleTrustManager extensibleTrustManager) {
    // this.extensibleTrustManager = extensibleTrustManager;
    // logger.debug("Registering Trust Manager Provider : {}", DEFAULT_TRUST_MANAGER_PROVIDER);
    // this.extensibleTrustManager.addTlsTrustManagerProvider(DEFAULT_TRUST_MANAGER_PROVIDER);
    // }
    //
    // public void unsetExtensibleTrustManager(ExtensibleTrustManager extensibleTrustManager) {
    // if (this.extensibleTrustManager == extensibleTrustManager) {
    // logger.debug("Unregistering Trust Manager Provider : {}", DEFAULT_TRUST_MANAGER_PROVIDER);
    // this.extensibleTrustManager.removeTlsTrustManagerProvider(DEFAULT_TRUST_MANAGER_PROVIDER);
    // this.extensibleTrustManager = null;
    // }
    // }

}
