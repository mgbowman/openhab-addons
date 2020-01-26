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
package org.openhab.binding.unifi.internal.ssl;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.net.http.TlsTrustManagerProvider;
import org.eclipse.smarthome.io.net.http.TrustAllTrustMananger;

/**
 *
 * The {@link UniFiTrustManagerProvider} is an implementation of {@link TlsTrustManagerProvider} which provides an
 * instance of {@link TrustAllTrustMananger} base on the peer's name.
 *
 * This is typically either the <code>CN</code> of the remote SSL certificate or the <code>host:port</code> combination
 * of the remote peer.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiTrustManagerProvider implements TlsTrustManagerProvider {

    private final String peer;

    public UniFiTrustManagerProvider(String peer) {
        this.peer = peer;
    }

    public UniFiTrustManagerProvider(String host, int port) {
        this(host + ":" + port);
    }

    @Override
    public String getHostName() {
        return peer;
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return TrustAllTrustMananger.getInstance();
    }

    @Override
    public String toString() {
        return String.format("UniFiTrustManagerProvider{peer: '%s'}", peer);
    }
}
