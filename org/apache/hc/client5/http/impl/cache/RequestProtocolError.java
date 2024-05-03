/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

enum RequestProtocolError {
    UNKNOWN,
    BODY_BUT_NO_LENGTH_ERROR,
    WEAK_ETAG_ON_PUTDELETE_METHOD_ERROR,
    WEAK_ETAG_AND_RANGE_ERROR,
    NO_CACHE_DIRECTIVE_WITH_FIELD_NAME;

}

