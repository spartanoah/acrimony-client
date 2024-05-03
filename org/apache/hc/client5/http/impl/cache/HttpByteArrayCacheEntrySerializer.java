/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheEntrySerializer;
import org.apache.hc.client5.http.cache.HttpCacheStorageEntry;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.CacheValidityPolicy;
import org.apache.hc.client5.http.impl.cache.CachedHttpResponseGenerator;
import org.apache.hc.core5.annotation.Experimental;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.impl.io.AbstractMessageParser;
import org.apache.hc.core5.http.impl.io.AbstractMessageWriter;
import org.apache.hc.core5.http.impl.io.DefaultHttpResponseParser;
import org.apache.hc.core5.http.impl.io.SessionOutputBufferImpl;
import org.apache.hc.core5.http.io.SessionInputBuffer;
import org.apache.hc.core5.http.io.SessionOutputBuffer;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.BasicLineFormatter;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.apache.hc.core5.util.TimeValue;

@Experimental
public class HttpByteArrayCacheEntrySerializer
implements HttpCacheEntrySerializer<byte[]> {
    public static final HttpByteArrayCacheEntrySerializer INSTANCE = new HttpByteArrayCacheEntrySerializer();
    private static final String SC_CACHE_ENTRY_PREFIX = "hc-";
    private static final String SC_HEADER_NAME_STORAGE_KEY = "hc-sk";
    private static final String SC_HEADER_NAME_RESPONSE_DATE = "hc-resp-date";
    private static final String SC_HEADER_NAME_REQUEST_DATE = "hc-req-date";
    private static final String SC_HEADER_NAME_NO_CONTENT = "hc-no-content";
    private static final String SC_HEADER_NAME_VARIANT_MAP_KEY = "hc-varmap-key";
    private static final String SC_HEADER_NAME_VARIANT_MAP_VALUE = "hc-varmap-val";
    private static final String SC_CACHE_ENTRY_PRESERVE_PREFIX = "hc-esc-";
    private static final int BUFFER_SIZE = 8192;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public byte[] serialize(HttpCacheStorageEntry httpCacheEntry) throws ResourceIOException {
        if (httpCacheEntry.getKey() == null) {
            throw new IllegalStateException("Cannot serialize cache object with null storage key");
        }
        BasicHttpRequest httpRequest = new BasicHttpRequest(httpCacheEntry.getContent().getRequestMethod(), "/");
        NoAgeCacheValidityPolicy cacheValidityPolicy = new NoAgeCacheValidityPolicy();
        CachedHttpResponseGenerator cachedHttpResponseGenerator = new CachedHttpResponseGenerator(cacheValidityPolicy);
        SimpleHttpResponse httpResponse = cachedHttpResponseGenerator.generateResponse(httpRequest, httpCacheEntry.getContent());
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();){
            int resourceLength;
            HttpByteArrayCacheEntrySerializer.escapeHeaders(httpResponse);
            this.addMetadataPseudoHeaders(httpResponse, httpCacheEntry);
            byte[] bodyBytes = httpResponse.getBodyBytes();
            if (bodyBytes == null) {
                httpResponse.addHeader(SC_HEADER_NAME_NO_CONTENT, Boolean.TRUE.toString());
                resourceLength = 0;
            } else {
                resourceLength = bodyBytes.length;
            }
            SessionOutputBufferImpl outputBuffer = new SessionOutputBufferImpl(8192);
            AbstractMessageWriter<SimpleHttpResponse> httpResponseWriter = this.makeHttpResponseWriter(outputBuffer);
            httpResponseWriter.write(httpResponse, (SessionOutputBuffer)outputBuffer, (OutputStream)out);
            outputBuffer.flush(out);
            byte[] headerBytes = out.toByteArray();
            byte[] bytes = new byte[headerBytes.length + resourceLength];
            System.arraycopy(headerBytes, 0, bytes, 0, headerBytes.length);
            if (resourceLength > 0) {
                System.arraycopy(bodyBytes, 0, bytes, headerBytes.length, resourceLength);
            }
            byte[] byArray = bytes;
            return byArray;
        } catch (IOException | HttpException e) {
            throw new ResourceIOException("Exception while serializing cache entry", e);
        }
    }

    /*
     * Exception decompiling
     */
    @Override
    public HttpCacheStorageEntry deserialize(byte[] serializedObject) throws ResourceIOException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 3 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:538)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         *     at async.DecompilerRunnable.cfrDecompilation(DecompilerRunnable.java:350)
         *     at async.DecompilerRunnable.call(DecompilerRunnable.java:311)
         *     at async.DecompilerRunnable.call(DecompilerRunnable.java:26)
         *     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
         *     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
         *     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
         *     at java.lang.Thread.run(Thread.java:750)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    protected AbstractMessageWriter<SimpleHttpResponse> makeHttpResponseWriter(SessionOutputBuffer outputBuffer) {
        return new SimpleHttpResponseWriter();
    }

    protected InputStream makeByteArrayInputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    protected AbstractMessageParser<ClassicHttpResponse> makeHttpResponseParser() {
        return new DefaultHttpResponseParser();
    }

    private static void escapeHeaders(HttpResponse httpResponse) {
        Header[] headers;
        for (Header header : headers = httpResponse.getHeaders()) {
            if (!header.getName().startsWith(SC_CACHE_ENTRY_PREFIX)) continue;
            httpResponse.removeHeader(header);
            httpResponse.addHeader(SC_CACHE_ENTRY_PRESERVE_PREFIX + header.getName(), header.getValue());
        }
    }

    private void unescapeHeaders(HttpResponse httpResponse) {
        Header[] headers;
        for (Header header : headers = httpResponse.getHeaders()) {
            if (!header.getName().startsWith(SC_CACHE_ENTRY_PRESERVE_PREFIX)) continue;
            httpResponse.removeHeader(header);
            httpResponse.addHeader(header.getName().substring(SC_CACHE_ENTRY_PRESERVE_PREFIX.length()), header.getValue());
        }
    }

    private void addMetadataPseudoHeaders(HttpResponse httpResponse, HttpCacheStorageEntry httpCacheEntry) {
        httpResponse.addHeader(SC_HEADER_NAME_STORAGE_KEY, httpCacheEntry.getKey());
        httpResponse.addHeader(SC_HEADER_NAME_RESPONSE_DATE, Long.toString(httpCacheEntry.getContent().getResponseDate().getTime()));
        httpResponse.addHeader(SC_HEADER_NAME_REQUEST_DATE, Long.toString(httpCacheEntry.getContent().getRequestDate().getTime()));
        for (Map.Entry<String, String> entry : httpCacheEntry.getContent().getVariantMap().entrySet()) {
            httpResponse.addHeader(SC_HEADER_NAME_VARIANT_MAP_KEY, entry.getKey());
            httpResponse.addHeader(SC_HEADER_NAME_VARIANT_MAP_VALUE, entry.getValue());
        }
    }

    private static String getCachePseudoHeaderAndRemove(HttpResponse response, String name) throws ResourceIOException {
        String headerValue = HttpByteArrayCacheEntrySerializer.getOptionalCachePseudoHeaderAndRemove(response, name);
        if (headerValue == null) {
            throw new ResourceIOException("Expected cache header '" + name + "' not found");
        }
        return headerValue;
    }

    private static String getOptionalCachePseudoHeaderAndRemove(HttpResponse response, String name) {
        Header header = response.getFirstHeader(name);
        if (header == null) {
            return null;
        }
        response.removeHeader(header);
        return header.getValue();
    }

    private static Date getCachePseudoHeaderDateAndRemove(HttpResponse response, String name) throws ResourceIOException {
        String value = HttpByteArrayCacheEntrySerializer.getCachePseudoHeaderAndRemove(response, name);
        response.removeHeaders(name);
        try {
            long timestamp = Long.parseLong(value);
            return new Date(timestamp);
        } catch (NumberFormatException e) {
            throw new ResourceIOException("Invalid value for header '" + name + "'", e);
        }
    }

    private static boolean getCachePseudoHeaderBooleanAndRemove(ClassicHttpResponse response, String name) {
        return Boolean.parseBoolean(HttpByteArrayCacheEntrySerializer.getOptionalCachePseudoHeaderAndRemove(response, name));
    }

    private static Map<String, String> getVariantMapPseudoHeadersAndRemove(HttpResponse response) throws ResourceIOException {
        Header[] headers = response.getHeaders();
        HashMap<String, String> variantMap = new HashMap<String, String>(0);
        String lastKey = null;
        for (Header header : headers) {
            if (header.getName().equals(SC_HEADER_NAME_VARIANT_MAP_KEY)) {
                lastKey = header.getValue();
                response.removeHeader(header);
                continue;
            }
            if (!header.getName().equals(SC_HEADER_NAME_VARIANT_MAP_VALUE)) continue;
            if (lastKey == null) {
                throw new ResourceIOException("Found mismatched variant map key/value headers");
            }
            variantMap.put(lastKey, header.getValue());
            lastKey = null;
            response.removeHeader(header);
        }
        if (lastKey != null) {
            throw new ResourceIOException("Found mismatched variant map key/value headers");
        }
        return variantMap;
    }

    private static void copyBytes(SessionInputBuffer srcBuf, InputStream src, OutputStream dest) throws IOException {
        int lastBytesRead;
        byte[] buf = new byte[8192];
        while ((lastBytesRead = srcBuf.read(buf, src)) != -1) {
            dest.write(buf, 0, lastBytesRead);
        }
    }

    private static class NoAgeCacheValidityPolicy
    extends CacheValidityPolicy {
        private NoAgeCacheValidityPolicy() {
        }

        @Override
        public TimeValue getCurrentAge(HttpCacheEntry entry, Date now) {
            return TimeValue.ZERO_MILLISECONDS;
        }
    }

    private class SimpleHttpResponseWriter
    extends AbstractMessageWriter<SimpleHttpResponse> {
        public SimpleHttpResponseWriter() {
            super(BasicLineFormatter.INSTANCE);
        }

        @Override
        protected void writeHeadLine(SimpleHttpResponse message, CharArrayBuffer lineBuf) {
            ProtocolVersion transportVersion = message.getVersion();
            BasicLineFormatter.INSTANCE.formatStatusLine(lineBuf, new StatusLine(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1, message.getCode(), message.getReasonPhrase()));
        }
    }
}

