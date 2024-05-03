/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.json;

import java.util.Enumeration;
import java.util.Properties;
import org.json.JSONException;
import org.json.JSONObject;

public class Property {
    public static JSONObject toJSONObject(Properties properties) throws JSONException {
        JSONObject jo = new JSONObject();
        if (properties != null && !properties.isEmpty()) {
            Enumeration<?> enumProperties = properties.propertyNames();
            while (enumProperties.hasMoreElements()) {
                String name = (String)enumProperties.nextElement();
                jo.put(name, properties.getProperty(name));
            }
        }
        return jo;
    }

    public static Properties toProperties(JSONObject jo) throws JSONException {
        Properties properties = new Properties();
        if (jo != null) {
            for (String key : jo.keySet()) {
                Object value = jo.opt(key);
                if (JSONObject.NULL.equals(value)) continue;
                properties.put(key, value.toString());
            }
        }
        return properties;
    }
}

