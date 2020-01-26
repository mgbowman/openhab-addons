package org.openhab.binding.unifi.internal.api.util;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.unifi.internal.api.model.UniFiSiteConfigSection;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public class UniFiSiteConfigDeserializer implements JsonDeserializer<UniFiSiteConfigSection> {

    private static final String KEY_ID = "_id";

    private static final String KEY_SITE_ID = "site_id";

    @Override
    public UniFiSiteConfigSection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

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
