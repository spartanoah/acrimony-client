/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.util.Identifiable;

@Internal
public final class ConnPoolSupport {
    public static String getId(Object object) {
        if (object == null) {
            return null;
        }
        return object instanceof Identifiable ? ((Identifiable)object).getId() : object.getClass().getSimpleName() + "-" + Integer.toHexString(System.identityHashCode(object));
    }

    public static String formatStats(HttpRoute route, Object state, ConnPoolControl<HttpRoute> connPool) {
        StringBuilder buf = new StringBuilder();
        buf.append("[route: ").append(route).append("]");
        if (state != null) {
            buf.append("[state: ").append(state).append("]");
        }
        PoolStats totals = connPool.getTotalStats();
        PoolStats stats = connPool.getStats(route);
        buf.append("[total available: ").append(totals.getAvailable()).append("; ");
        buf.append("route allocated: ").append(stats.getLeased() + stats.getAvailable());
        buf.append(" of ").append(stats.getMax()).append("; ");
        buf.append("total allocated: ").append(totals.getLeased() + totals.getAvailable());
        buf.append(" of ").append(totals.getMax()).append("]");
        return buf.toString();
    }
}

