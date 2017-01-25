/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link UniFiResponse} class wraps a list of <T> data instances
 * returned from the various API calls to the UniFi controller.
 *
 * @author Matthew Bowman - Initial contribution
 *
 * @param <T> The concrete response data class
 */
public class UniFiResponse<T> {

    private List<T> data = new ArrayList<T>();

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

}
