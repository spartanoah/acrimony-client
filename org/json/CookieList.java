/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.json;

import org.json.Cookie;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CookieList {
    public static JSONObject toJSONObject(String string) throws JSONException {
        JSONObject jo = new JSONObject();
        JSONTokener x = new JSONTokener(string);
        while (x.more()) {
            String name = Cookie.unescape(x.nextTo('='));
            x.next('=');
            jo.put(name, Cookie.unescape(x.nextTo(';')));
            x.next();
        }
        return jo;
    }

    public static String toString(JSONObject jo) throws JSONException {
        boolean isEndOfPair = false;
        StringBuilder sb = new StringBuilder();
        for (String key : jo.keySet()) {
            Object value = jo.opt(key);
            if (JSONObject.NULL.equals(value)) continue;
            if (isEndOfPair) {
                sb.append(';');
            }
            sb.append(Cookie.escape(key));
            sb.append("=");
            sb.append(Cookie.escape(value.toString()));
            isEndOfPair = true;
        }
        return sb.toString();
    }
}

