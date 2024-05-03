/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.util.HashMap;
import java.util.Map;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.BackoffManager;
import org.apache.hc.client5.http.impl.classic.Clock;
import org.apache.hc.client5.http.impl.classic.SystemClock;
import org.apache.hc.core5.annotation.Experimental;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

@Experimental
public class AIMDBackoffManager
implements BackoffManager {
    private final ConnPoolControl<HttpRoute> connPerRoute;
    private final Clock clock;
    private final Map<HttpRoute, Long> lastRouteProbes;
    private final Map<HttpRoute, Long> lastRouteBackoffs;
    private TimeValue coolDown = TimeValue.ofSeconds(5L);
    private double backoffFactor = 0.5;
    private int cap = 2;

    public AIMDBackoffManager(ConnPoolControl<HttpRoute> connPerRoute) {
        this(connPerRoute, new SystemClock());
    }

    AIMDBackoffManager(ConnPoolControl<HttpRoute> connPerRoute, Clock clock) {
        this.clock = clock;
        this.connPerRoute = connPerRoute;
        this.lastRouteProbes = new HashMap<HttpRoute, Long>();
        this.lastRouteBackoffs = new HashMap<HttpRoute, Long>();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void backOff(HttpRoute route) {
        ConnPoolControl<HttpRoute> connPoolControl = this.connPerRoute;
        synchronized (connPoolControl) {
            int curr = this.connPerRoute.getMaxPerRoute(route);
            Long lastUpdate = this.getLastUpdate(this.lastRouteBackoffs, route);
            long now = this.clock.getCurrentTime();
            if (now - lastUpdate < this.coolDown.toMilliseconds()) {
                return;
            }
            this.connPerRoute.setMaxPerRoute(route, this.getBackedOffPoolSize(curr));
            this.lastRouteBackoffs.put(route, now);
        }
    }

    private int getBackedOffPoolSize(int curr) {
        if (curr <= 1) {
            return 1;
        }
        return (int)Math.floor(this.backoffFactor * (double)curr);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void probe(HttpRoute route) {
        ConnPoolControl<HttpRoute> connPoolControl = this.connPerRoute;
        synchronized (connPoolControl) {
            int curr = this.connPerRoute.getMaxPerRoute(route);
            int max = curr >= this.cap ? this.cap : curr + 1;
            Long lastProbe = this.getLastUpdate(this.lastRouteProbes, route);
            Long lastBackoff = this.getLastUpdate(this.lastRouteBackoffs, route);
            long now = this.clock.getCurrentTime();
            if (now - lastProbe < this.coolDown.toMilliseconds() || now - lastBackoff < this.coolDown.toMilliseconds()) {
                return;
            }
            this.connPerRoute.setMaxPerRoute(route, max);
            this.lastRouteProbes.put(route, now);
        }
    }

    private Long getLastUpdate(Map<HttpRoute, Long> updates, HttpRoute route) {
        Long lastUpdate = updates.get(route);
        if (lastUpdate == null) {
            lastUpdate = 0L;
        }
        return lastUpdate;
    }

    public void setBackoffFactor(double d) {
        Args.check(d > 0.0 && d < 1.0, "Backoff factor must be 0.0 < f < 1.0");
        this.backoffFactor = d;
    }

    public void setCoolDown(TimeValue coolDown) {
        Args.positive(coolDown.getDuration(), "coolDown");
        this.coolDown = coolDown;
    }

    public void setPerHostConnectionCap(int cap) {
        Args.positive(cap, "Per host connection cap");
        this.cap = cap;
    }
}

