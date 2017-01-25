/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.unifi.internal.api.UniFiClient;
import org.openhab.binding.unifi.internal.api.UniFiClientResponse;
import org.openhab.binding.unifi.internal.api.UniFiDevice;
import org.openhab.binding.unifi.internal.api.UniFiDeviceResponse;
import org.openhab.binding.unifi.internal.ssl.UniFiHostnameVerifier;
import org.openhab.binding.unifi.internal.ssl.UniFiTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UniFiController {

    private Logger logger = LoggerFactory.getLogger(UniFiController.class);

    private UniFiControllerConfig config;

    private HostnameVerifier hostnameVerifier = new UniFiHostnameVerifier();

    private SSLSocketFactory sslSocketFactory;

    private Map<String, UniFiClient> clientMap;

    private ObjectMapper mapper = new ObjectMapper();

    private CookieManager cookieManager = new CookieManager();

    public UniFiController(UniFiControllerConfig config) {
        this.config = config;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new UniFiTrustManager() }, new SecureRandom());
            sslSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public synchronized UniFiControllerConfig getConfig() {
        return config;
    }

    public synchronized void setConfig(UniFiControllerConfig config) {
        this.config = config;
    }

    private HttpsURLConnection openConnection(String uri) throws IOException, URISyntaxException {
        URL url = new URL(uri);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
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
        if (logger.isDebugEnabled()) {
            logger.debug("Sending the following headers to {}", url);
            Map<String, List<String>> headers = connection.getRequestProperties();
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                logger.debug("{} : {}", header.getKey(), header.getValue());
            }
        }
        return connection;
    }

    private String get(String url) throws IOException, URISyntaxException {
        return get(url, (Map<String, String>) null);
    }

    private String get(String url, Map<String, String> params) throws IOException, URISyntaxException {
        String response = null;
        HttpsURLConnection connection = openConnection(url);
        if (params != null) {
            connection.setDoOutput(true);
            mapper.writeValue(connection.getOutputStream(), params);
        }
        int rc = connection.getResponseCode();
        if (rc == HTTP_OK) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received the following headers from {}", url);
                Map<String, List<String>> headers = connection.getHeaderFields();
                for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                    logger.debug("{} : {}", header.getKey(), header.getValue());
                }
            }
            cookieManager.put(connection.getURL().toURI(), connection.getHeaderFields());
            response = IOUtils.toString(connection.getInputStream());
        }
        return response;
    }

    private <T> T get(String url, Class<T> responseType) throws IOException, URISyntaxException {
        T response = null;
        response = mapper.readValue(get(url), responseType);
        return response;
    }

    public synchronized boolean login() {
        boolean success = false;
        // login
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", config.getUsername());
        params.put("password", config.getPassword());
        try {
            String response = get(config.getLoginUrl(), params);
            success = (response != null);
        } catch (Exception e) {
            success = false;
        }
        return success;
    }

    public synchronized Map<String, UniFiDevice> getDevices() {
        Map<String, UniFiDevice> deviceMap = null;
        try {
            UniFiDeviceResponse response = get(config.getDevicesUrl(), UniFiDeviceResponse.class);
            if (response != null) {
                deviceMap = new HashMap<String, UniFiDevice>();
                List<UniFiDevice> devices = response.getData();
                for (UniFiDevice device : devices) {
                    deviceMap.put(device.getMac(), device);
                    logger.debug("UniFi Device: {} (mac = {})", device.getName(), device.getMac());
                }
            }
        } catch (Exception e) {
            deviceMap = null;
        }
        return deviceMap;
    }

    private synchronized Map<String, UniFiClient> getClients() {
        if (clientMap == null) {
            try {
                UniFiClientResponse response = get(config.getClientsUrl(), UniFiClientResponse.class);
                if (response != null) {
                    clientMap = new HashMap<String, UniFiClient>();
                    List<UniFiClient> clients = response.getData();
                    Map<String, UniFiDevice> devices = getDevices();
                    for (UniFiClient client : clients) {
                        client.setAp(devices.get(client.getApMac()));
                        clientMap.put(client.getMac(), client);
                        logger.debug("UniFi Client: {} (mac = {}, essid = {}, ap = {}, rssi = {})",
                                client.getHostname(), client.getMac(), client.getEssid(), client.getAp().getName(),
                                client.getRssi());
                    }
                }
            } catch (Exception e) {
                clientMap = null;
            }
        }
        return clientMap;
    }

    public synchronized UniFiClient getClient(String mac) {
        UniFiClient client = null;
        Map<String, UniFiClient> clients = getClients();
        if (clients != null) {
            client = clients.get(mac);
        }
        return client;
    }

    public synchronized void logout() {
        try {
            get(config.getLogoutUrl());
        } catch (Exception e) {
            // nop
        }
        clientMap = null;
    }

}
