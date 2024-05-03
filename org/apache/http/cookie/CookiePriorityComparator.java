/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.cookie;

import java.util.Comparator;
import java.util.Date;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class CookiePriorityComparator
implements Comparator<Cookie> {
    public static final CookiePriorityComparator INSTANCE = new CookiePriorityComparator();

    private int getPathLength(Cookie cookie) {
        String path = cookie.getPath();
        return path != null ? path.length() : 1;
    }

    @Override
    public int compare(Cookie c1, Cookie c2) {
        int l1 = this.getPathLength(c1);
        int l2 = this.getPathLength(c2);
        int result = l2 - l1;
        if (result == 0 && c1 instanceof BasicClientCookie && c2 instanceof BasicClientCookie) {
            Date d1 = ((BasicClientCookie)c1).getCreationDate();
            Date d2 = ((BasicClientCookie)c2).getCreationDate();
            if (d1 != null && d2 != null) {
                return (int)(d1.getTime() - d2.getTime());
            }
        }
        return result;
    }
}

