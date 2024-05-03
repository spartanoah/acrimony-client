/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.socksx.v5.AbstractSocks5Message;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultSocks5InitialRequest
extends AbstractSocks5Message
implements Socks5InitialRequest {
    private final List<Socks5AuthMethod> authMethods;

    public DefaultSocks5InitialRequest(Socks5AuthMethod ... authMethods) {
        ObjectUtil.checkNotNull(authMethods, "authMethods");
        ArrayList<Socks5AuthMethod> list = new ArrayList<Socks5AuthMethod>(authMethods.length);
        for (Socks5AuthMethod m : authMethods) {
            if (m == null) break;
            list.add(m);
        }
        this.authMethods = Collections.unmodifiableList(ObjectUtil.checkNonEmpty(list, "list"));
    }

    public DefaultSocks5InitialRequest(Iterable<Socks5AuthMethod> authMethods) {
        ObjectUtil.checkNotNull(authMethods, "authSchemes");
        ArrayList<Socks5AuthMethod> list = new ArrayList<Socks5AuthMethod>();
        for (Socks5AuthMethod m : authMethods) {
            if (m == null) break;
            list.add(m);
        }
        this.authMethods = Collections.unmodifiableList(ObjectUtil.checkNonEmpty(list, "list"));
    }

    @Override
    public List<Socks5AuthMethod> authMethods() {
        return this.authMethods;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(StringUtil.simpleClassName(this));
        DecoderResult decoderResult = this.decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", authMethods: ");
        } else {
            buf.append("(authMethods: ");
        }
        buf.append(this.authMethods());
        buf.append(')');
        return buf.toString();
    }
}

