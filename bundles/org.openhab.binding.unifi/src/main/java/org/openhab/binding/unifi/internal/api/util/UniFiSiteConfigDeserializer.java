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
package org.openhab.binding.unifi.internal.api.util;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.model.UniFiSiteConfigSection;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 *
 * The {@link UniFiSiteConfigDeserializer} is an implementation of {@link JsonDeserializer} that deserializes
 * instances of {@link UniFiSiteConfigSection}
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiSiteConfigDeserializer implements JsonDeserializer<UniFiSiteConfigSection> {

    private static final String KEY_ID = "_id";

    private static final String KEY_SITE_ID = "site_id";

    @Override
    public UniFiSiteConfigSection deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> config = new HashMap<>();

        JsonObject jsonObject = json.getAsJsonObject();
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {

            // mgb: skip redundant keys
            if (KEY_ID.equals(key) || KEY_SITE_ID.contentEquals(key)) {
                continue;
            }

            Object value = null;

            // mgb: handle primitives
            if (jsonObject.get(key).isJsonPrimitive()) {
                JsonPrimitive primitive = (JsonPrimitive) jsonObject.get(key);
                if (primitive.isBoolean()) {
                    value = primitive.getAsBoolean();
                } else if (primitive.isNumber()) {
                    value = primitive.getAsNumber();
                } else if (primitive.isString()) {
                    value = primitive.getAsString();
                } else {
                    // TODO mgb: what happens here???
                }
            }

            // TODO mgb: handle arrays

            // TODO mgb: handle objects

            if (value != null) {
                config.put(key, value);
            }
        }

        return new UniFiSiteConfigSection(config);
    }

}
