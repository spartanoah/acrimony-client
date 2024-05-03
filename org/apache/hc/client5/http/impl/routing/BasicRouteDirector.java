/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.routing;

import org.apache.hc.client5.http.RouteInfo;
import org.apache.hc.client5.http.routing.HttpRouteDirector;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public class BasicRouteDirector
implements HttpRouteDirector {
    @Override
    public int nextStep(RouteInfo plan, RouteInfo fact) {
        Args.notNull(plan, "Planned route");
        int step = -1;
        step = fact == null || fact.getHopCount() < 1 ? this.firstStep(plan) : (plan.getHopCount() > 1 ? this.proxiedStep(plan, fact) : this.directStep(plan, fact));
        return step;
    }

    protected int firstStep(RouteInfo plan) {
        return plan.getHopCount() > 1 ? 2 : 1;
    }

    protected int directStep(RouteInfo plan, RouteInfo fact) {
        if (fact.getHopCount() > 1) {
            return -1;
        }
        if (!plan.getTargetHost().equals(fact.getTargetHost())) {
            return -1;
        }
        if (plan.isSecure() != fact.isSecure()) {
            return -1;
        }
        if (plan.getLocalAddress() != null && !plan.getLocalAddress().equals(fact.getLocalAddress())) {
            return -1;
        }
        return 0;
    }

    protected int proxiedStep(RouteInfo plan, RouteInfo fact) {
        int fhc;
        if (fact.getHopCount() <= 1) {
            return -1;
        }
        if (!plan.getTargetHost().equals(fact.getTargetHost())) {
            return -1;
        }
        int phc = plan.getHopCount();
        if (phc < (fhc = fact.getHopCount())) {
            return -1;
        }
        for (int i = 0; i < fhc - 1; ++i) {
            if (plan.getHopTarget(i).equals(fact.getHopTarget(i))) continue;
            return -1;
        }
        if (phc > fhc) {
            return 4;
        }
        if (fact.isTunnelled() && !plan.isTunnelled() || fact.isLayered() && !plan.isLayered()) {
            return -1;
        }
        if (plan.isTunnelled() && !fact.isTunnelled()) {
            return 3;
        }
        if (plan.isLayered() && !fact.isLayered()) {
            return 5;
        }
        if (plan.isSecure() != fact.isSecure()) {
            return -1;
        }
        return 0;
    }
}

