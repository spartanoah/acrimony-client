/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ipfilter;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.AbstractRemoteAddressFilter;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.util.internal.ObjectUtil;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@ChannelHandler.Sharable
public class RuleBasedIpFilter
extends AbstractRemoteAddressFilter<InetSocketAddress> {
    private final boolean acceptIfNotFound;
    private final List<IpFilterRule> rules;

    public RuleBasedIpFilter(IpFilterRule ... rules) {
        this(true, rules);
    }

    public RuleBasedIpFilter(boolean acceptIfNotFound, IpFilterRule ... rules) {
        ObjectUtil.checkNotNull(rules, "rules");
        this.acceptIfNotFound = acceptIfNotFound;
        this.rules = new ArrayList<IpFilterRule>(rules.length);
        for (IpFilterRule rule : rules) {
            if (rule == null) continue;
            this.rules.add(rule);
        }
    }

    @Override
    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
        for (IpFilterRule rule : this.rules) {
            if (!rule.matches(remoteAddress)) continue;
            return rule.ruleType() == IpFilterRuleType.ACCEPT;
        }
        return this.acceptIfNotFound;
    }
}

