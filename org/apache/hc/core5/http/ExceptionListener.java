/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpConnection;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface ExceptionListener {
    public static final ExceptionListener NO_OP = new ExceptionListener(){

        @Override
        public void onError(Exception ex) {
        }

        @Override
        public void onError(HttpConnection connection, Exception ex) {
        }
    };
    public static final ExceptionListener STD_ERR = new ExceptionListener(){

        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onError(HttpConnection connection, Exception ex) {
            ex.printStackTrace();
        }
    };

    public void onError(Exception var1);

    public void onError(HttpConnection var1, Exception var2);
}

