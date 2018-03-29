/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal;

import static java.net.HttpURLConnection.*;

import java.io.IOException;
import java.net.CookieManager;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.openhab.binding.unifi.internal.api.UniFiClient;
import org.openhab.binding.unifi.internal.api.UniFiDevice;
import org.openhab.binding.unifi.internal.api.UniFiSite;
import org.openhab.binding.unifi.internal.api.UniFiWiredClient;
import org.openhab.binding.unifi.internal.api.UniFiWirelessClient;
import org.openhab.binding.unifi.internal.api.json.UniFiClientResponse;
import org.openhab.binding.unifi.internal.api.json.UniFiDeviceResponse;
import org.openhab.binding.unifi.internal.api.json.UniFiSiteResponse;
import org.openhab.binding.unifi.internal.ssl.UniFiHostnameVerifier;
import org.openhab.binding.unifi.internal.ssl.UniFiTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link UniFiController} is the main communication point with an external instance of the Ubiquiti Networks
 * Controller Software.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiController {

    private static final Map<String, String> INSIGHTS_WITHIN_24H = Collections.singletonMap("within", "24");

    private final Logger logger = LoggerFactory.getLogger(UniFiController.class);

    private UniFiControllerConfig config;

    private HostnameVerifier hostnameVerifier = new UniFiHostnameVerifier();

    private SSLSocketFactory sslSocketFactory;

    private Map<String, UniFiSite> sitesCache = Collections.emptyMap();

    private Map<String, UniFiDevice> devicesCache = Collections.emptyMap();

    private Map<String, UniFiClient> clientsCache = Collections.emptyMap();

    private Map<String, UniFiClient> insightsCache = Collections.emptyMap();

    private ObjectMapper objectMapper = new ObjectMapper();

    private CookieManager cookieManager = new CookieManager();

    public UniFiController(UniFiControllerConfig config) throws UniFiException {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new UniFiTrustManager() }, new SecureRandom());
            sslSocketFactory = sc.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new UniFiException("Could not install SSL trust-all manager", e);
        }
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        if (!config.isValid()) {
            throw new UniFiException("Invalid configuration");
        } else if (config.getConsiderHome() <= config.getRefresh()) {
            throw new UniFiException(
                    "Invalid configuration: consider home interval must be larger than refresh interval");
        }

        this.config = config;
    }

    // Private API Functions

    private String getBaseUrl() {
        return "https://" + config.getHost() + ":" + config.getPort() + "/";
    }

    private String getLoginUrl() {
        return getBaseUrl() + "api/login";
    }

    private String getLogoutUrl() {
        return getBaseUrl() + "logout";
    }

    private String getSitesUrl() {
        return getBaseUrl() + "api/self/sites";
    }

    private String getDevicesUrl(UniFiSite site) {
        return getBaseUrl() + "api/s/" + site.getPath() + "/stat/device";
    }

    private String getClientsUrl(UniFiSite site) {
        return getBaseUrl() + "api/s/" + site.getPath() + "/stat/sta";
    }

    private String getInsightsUrl(UniFiSite site) {
        return getBaseUrl() + "api/s/" + site.getPath() + "/stat/alluser";
    }

    private HttpsURLConnection openConnection(String uri) throws IOException, URISyntaxException {
        URL url = new URL(uri);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(5000); // 5s timeout
        connection.setSSLSocketFactory(sslSocketFactory);
        connection.setHostnameVerifier(hostnameVerifier);
        Map<String, List<String>> cookies = cookieManager.get(url.toURI(), connection.getRequestProperties());
        for (Map.Entry<String, List<String>> cookie : cookies.entrySet()) {
            String key = cookie.getKey();
            List<String> values = cookie.getValue();
            for (String value : values) {
                connection.setRequestProperty(key, value);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Sending the following headers to {}", url);
            Map<String, List<String>> headers = connection.getRequestProperties();
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                logger.trace("{} : {}", header.getKey(), header.getValue());
            }
        }
        return connection;
    }

    private String get(String url, Map<String, String> params, boolean loginGuard)
            throws IOException, URISyntaxException {
        String response = null;
        HttpsURLConnection connection = openConnection(url);
        if (params != null) {
            connection.setDoOutput(true);
            objectMapper.writeValue(connection.getOutputStream(), params);
        }
        int rc = connection.getResponseCode();
        if (rc == HTTP_OK) {
            if (logger.isTraceEnabled()) {
                logger.trace("Received the following headers from {}", url);
                Map<String, List<String>> headers = connection.getHeaderFields();
                for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                    logger.trace("{} : {}", header.getKey(), header.getValue());
                }
            }
            cookieManager.put(connection.getURL().toURI(), connection.getHeaderFields());
            response = IOUtils.toString(connection.getInputStream());
        } else if (rc == HTTP_UNAUTHORIZED && !loginGuard) {
            // our session has timed-out, let's try again
            if (login()) {
                return get(url, params, true); // guard against a "login loop"
            }
        }
        return response;
    }

    private String get(String url, Map<String, String> params) throws IOException, URISyntaxException {
        return get(url, params, false);
    }

    private String get(String url) throws IOException, URISyntaxException {
        return get(url, (Map<String, String>) null);
    }

    private <T> T get(String url, Map<String, String> params, Class<T> responseType) throws UniFiException {
        T response = null;
        try {
            String content = get(url, params);
            if (StringUtils.isBlank(content)) {
                logger.warn("Empty GET response from URL: {}", url);
                throw new UniFiException("Could not communicate with the UniFi Controller");
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("JSON content from URL: {}\n{}", url, objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(objectMapper.readValue(content, Object.class)));
                }
                response = objectMapper.readValue(content, responseType);
            }
        } catch (SocketException | SocketTimeoutException e) {
            throw new UniFiException("Could not communicate with the UniFi Controller");
        } catch (Exception e) {
            logger.error("Error GET'ing URL: {}", url, e);
            throw new UniFiException(e);
        }
        return response;
    }

    private <T> T get(String url, Class<T> responseType) throws UniFiException {
        return get(url, null, responseType);
    }

    private Map<String, UniFiSite> getSites() throws UniFiException {
        UniFiSiteResponse response = get(getSitesUrl(), UniFiSiteResponse.class);
        Map<String, UniFiSite> siteMap = new HashMap<>();
        List<UniFiSite> sites = response.getData();
        logger.debug("Found {} UniFi Site(s):", sites.size());
        for (UniFiSite site : sites) {
            logger.debug("  {}", site);
            siteMap.put(site.getId(), site);
        }
        return siteMap;
    }

    private Map<String, UniFiDevice> getDevices(UniFiSite site) throws UniFiException {
        UniFiDeviceResponse response = get(getDevicesUrl(site), UniFiDeviceResponse.class);
        Map<String, UniFiDevice> deviceMap = new HashMap<>();
        List<UniFiDevice> devices = response.getData();
        logger.debug("Found {} UniFi Device(s):", devices.size());
        for (UniFiDevice device : devices) {
            device.setSite(sitesCache.get(device.getSiteId()));
            logger.debug("  {}", device);
            deviceMap.put(device.getMac(), device);
        }
        return deviceMap;
    }

    private Map<String, UniFiDevice> getDevices() throws UniFiException {
        Map<String, UniFiDevice> deviceMap = new HashMap<>();
        Collection<UniFiSite> sites = sitesCache.values();
        for (UniFiSite site : sites) {
            Map<String, UniFiDevice> devices = getDevices(site);
            deviceMap.putAll(devices);
        }
        return deviceMap;
    }

    private Map<String, UniFiClient> getClients(UniFiSite site) throws UniFiException {
        UniFiClientResponse response = get(getClientsUrl(site), UniFiClientResponse.class);
        Map<String, UniFiClient> clientMap = new HashMap<>();
        List<UniFiClient> clients = response.getData();
        logger.debug("Found {} UniFi Client(s):", clients.size());
        for (UniFiClient client : clients) {
            client.setDevice(devicesCache.get(client.getDeviceMac()));
            logger.debug("  {}", client);
            clientMap.put(client.getMac(), client);
        }
        return clientMap;
    }

    private Map<String, UniFiClient> getClients() throws UniFiException {
        Map<String, UniFiClient> clientMap = Collections.emptyMap();
        Collection<UniFiSite> sites = sitesCache.values();
        if (!sites.isEmpty()) {
            clientMap = new HashMap<>();
            for (UniFiSite site : sites) {
                Map<String, UniFiClient> siteClientMap = getClients(site);
                if (siteClientMap != null) {
                    clientMap.putAll(siteClientMap);
                }
            }
        }
        return clientMap;
    }

    private Map<String, UniFiClient> getInsights(UniFiSite site) throws UniFiException {
        UniFiClientResponse response = get(getInsightsUrl(site), INSIGHTS_WITHIN_24H, UniFiClientResponse.class);
        Map<String, UniFiClient> insightsMap = new HashMap<>();
        List<UniFiClient> clients = response.getData();
        logger.debug("Found {} UniFi Insights(s):", clients.size());
        for (UniFiClient client : clients) {
            logger.debug("  {}", client);
            insightsMap.put(client.getMac(), client);
        }
        return insightsMap;
    }

    private Map<String, UniFiClient> getInsights() throws UniFiException {
        Map<String, UniFiClient> insightsMap = Collections.emptyMap();
        Collection<UniFiSite> sites = sitesCache.values();
        if (!sites.isEmpty()) {
            insightsMap = new HashMap<>();
            for (UniFiSite site : sites) {
                Map<String, UniFiClient> siteInsightsMap = getInsights(site);
                if (siteInsightsMap != null) {
                    insightsMap.putAll(siteInsightsMap);
                }
            }
        }
        return insightsMap;
    }

    private boolean belongsToSite(UniFiClient client, String siteName) {
        boolean result = true; // mgb: assume true = proof by contradiction
        if (StringUtils.isNotEmpty(siteName)) {
            UniFiSite s = sitesCache.get(client.getSiteId());
            // mgb: if the 'site' can't be found or the name doesn't match...
            if (s == null || !s.matchesName(siteName)) {
                // mgb: ... then the client doesn't belong to this thing's configured 'site' and we 'filter' it
                result = false;
            }
        }
        return result;
    }

    // Public API functions

    public synchronized void refresh() throws UniFiException {
        sitesCache = getSites();
        devicesCache = getDevices();
        clientsCache = getClients();
        insightsCache = getInsights();
    }

    public synchronized UniFiWiredClient getWiredClient(String mac, String site) {
        UniFiClient client = clientsCache.get(mac);

        // mgb: short circuit
        if (client == null || BooleanUtils.isNotTrue(client.isWired()) || !belongsToSite(client, site)) {
            return null;
        }

        // mgb: instanceof check just for type / cast safety
        return (client instanceof UniFiWiredClient ? (UniFiWiredClient) client : null);
    }

    public synchronized UniFiWirelessClient getWirelessClient(String mac, String site) {
        // mgb: first check active clients and fallback to insights if not found
        UniFiClient client = clientsCache.containsKey(mac) ? clientsCache.get(mac) : insightsCache.get(mac);

        // mgb: short circuit
        if (client == null || BooleanUtils.isNotTrue(client.isWireless()) || !belongsToSite(client, site)) {
            return null;
        }

        // mgb: instanceof check just for type / cast safety
        return (client instanceof UniFiWirelessClient ? (UniFiWirelessClient) client : null);
    }

    public boolean isWirelessClientOnline(UniFiWirelessClient client) {
        boolean online = false;
        if (client != null) {
            Calendar lastSeen = client.getLastSeen();
            if (lastSeen == null) {
                logger.warn("Could not determine if client is online: mac = {}, lastSeen = null", client.getMac());
            } else {
                Calendar considerHome = (Calendar) lastSeen.clone();
                considerHome.add(Calendar.SECOND, config.getConsiderHome());
                Calendar now = Calendar.getInstance();
                online = (now.compareTo(considerHome) < 0);
            }
        }
        return online;
    }

    public boolean login() {
        boolean success = false;
        // login
        Map<String, String> params = new HashMap<>();
        params.put("username", config.getUsername());
        params.put("password", config.getPassword());
        try {
            String response = get(getLoginUrl(), params);
            success = (response != null);
        } catch (SocketException | SocketTimeoutException e) {
            logger.warn("Could not connect to the UniFi Controller");
            success = false;
        } catch (Exception e) {
            logger.error("Error trying to authenticate to UniFi Controller", e);
            success = false;
        }
        return success;
    }

    public void logout() {
        try {
            get(getLogoutUrl());
        } catch (Exception e) {
            // nop
        }
    }

}
