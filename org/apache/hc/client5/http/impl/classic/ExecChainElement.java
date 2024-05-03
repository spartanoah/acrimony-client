/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;

class ExecChainElement {
    private final ExecChainHandler handler;
    private final ExecChainElement next;

    ExecChainElement(ExecChainHandler handler, ExecChainElement next) {
        this.handler = handler;
        this.next = next;
    }

    public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope) throws IOException, HttpException {
        return this.handler.execute(request, scope, new ExecChain(){

            @Override
            public ClassicHttpResponse proceed(ClassicHttpRequest request, ExecChain.Scope scope) throws IOException, HttpException {
                return ExecChainElement.this.next.execute(request, scope);
            }
        });
    }

    public String toString() {
        return "{handler=" + this.handler.getClass() + ", next=" + (this.next != null ? this.next.handler.getClass() : "null") + '}';
    }
}

