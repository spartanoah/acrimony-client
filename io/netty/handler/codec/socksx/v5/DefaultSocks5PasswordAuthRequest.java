/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.socksx.v5.AbstractSocks5Message;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequest;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

public class DefaultSocks5PasswordAuthRequest
extends AbstractSocks5Message
implements Socks5PasswordAuthRequest {
    private final String username;
    private final String password;

    public DefaultSocks5PasswordAuthRequest(String username, String password) {
        ObjectUtil.checkNotNull(username, "username");
        ObjectUtil.checkNotNull(password, "password");
        if (username.length() > 255) {
            throw new IllegalArgumentException("username: **** (expected: less than 256 chars)");
        }
        if (password.length() > 255) {
            throw new IllegalArgumentException("password: **** (expected: less than 256 chars)");
        }
        this.username = username;
        this.password = password;
    }

    @Override
    public String username() {
        return this.username;
    }

    @Override
    public String password() {
        return this.password;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(StringUtil.simpleClassName(this));
        DecoderResult decoderResult = this.decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", username: ");
        } else {
            buf.append("(username: ");
        }
        buf.append(this.username());
        buf.append(", password: ****)");
        return buf.toString();
    }
}

