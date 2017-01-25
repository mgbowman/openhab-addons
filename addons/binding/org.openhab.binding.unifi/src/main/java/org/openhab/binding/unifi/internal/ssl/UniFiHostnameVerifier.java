/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * The {@link UniFiHostnameVerifier} implements a blind / trust-all hostname verifier
 * since the majority of UniFi controller installations are on private networks
 * without a valid hostname.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }

}
