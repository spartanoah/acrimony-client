/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.util.Iterator;
import org.apache.hc.client5.http.impl.MessageCopier;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

public final class ClassicRequestCopier
implements MessageCopier<ClassicHttpRequest> {
    public static final ClassicRequestCopier INSTANCE = new ClassicRequestCopier();

    @Override
    public ClassicHttpRequest copy(ClassicHttpRequest original) {
        if (original == null) {
            return null;
        }
        BasicClassicHttpRequest copy = new BasicClassicHttpRequest(original.getMethod(), original.getPath());
        copy.setVersion(original.getVersion());
        Iterator<Header> it = original.headerIterator();
        while (it.hasNext()) {
            copy.addHeader(it.next());
        }
        copy.setScheme(original.getScheme());
        copy.setAuthority(original.getAuthority());
        copy.setEntity(original.getEntity());
        return copy;
    }
}

