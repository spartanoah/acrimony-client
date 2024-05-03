/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.hc.client5.http.auth.AuthChallenge;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.AuthenticationException;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.MalformedChallengeException;
import org.apache.hc.client5.http.impl.auth.HttpEntityDigester;
import org.apache.hc.client5.http.impl.auth.UnsupportedDigestAlgorithmException;
import org.apache.hc.client5.http.utils.ByteArrayBuilder;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicHeaderValueFormatter;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigestScheme
implements AuthScheme,
Serializable {
    private static final long serialVersionUID = 3883908186234566916L;
    private static final Logger LOG = LoggerFactory.getLogger(DigestScheme.class);
    private static final char[] HEXADECIMAL = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final int QOP_UNKNOWN = -1;
    private static final int QOP_MISSING = 0;
    private static final int QOP_AUTH_INT = 1;
    private static final int QOP_AUTH = 2;
    private final Map<String, String> paramMap = new HashMap<String, String>();
    private boolean complete = false;
    private transient ByteArrayBuilder buffer;
    private String lastNonce;
    private long nounceCount;
    private String cnonce;
    private byte[] a1;
    private byte[] a2;
    private String username;
    private char[] password;

    public void initPreemptive(Credentials credentials, String cnonce, String realm) {
        Args.notNull(credentials, "Credentials");
        this.username = credentials.getUserPrincipal().getName();
        this.password = credentials.getPassword();
        this.paramMap.put("cnonce", cnonce);
        this.paramMap.put("realm", realm);
    }

    @Override
    public String getName() {
        return "Digest";
    }

    @Override
    public boolean isConnectionBased() {
        return false;
    }

    @Override
    public String getRealm() {
        return this.paramMap.get("realm");
    }

    @Override
    public void processChallenge(AuthChallenge authChallenge, HttpContext context) throws MalformedChallengeException {
        Args.notNull(authChallenge, "AuthChallenge");
        this.paramMap.clear();
        List<NameValuePair> params = authChallenge.getParams();
        if (params != null) {
            for (NameValuePair param : params) {
                this.paramMap.put(param.getName().toLowerCase(Locale.ROOT), param.getValue());
            }
        }
        if (this.paramMap.isEmpty()) {
            throw new MalformedChallengeException("Missing digest auth parameters");
        }
        this.complete = true;
    }

    @Override
    public boolean isChallengeComplete() {
        String s = this.paramMap.get("stale");
        return !"true".equalsIgnoreCase(s) && this.complete;
    }

    @Override
    public boolean isResponseReady(HttpHost host, CredentialsProvider credentialsProvider, HttpContext context) throws AuthenticationException {
        Args.notNull(host, "Auth host");
        Args.notNull(credentialsProvider, "CredentialsProvider");
        AuthScope authScope = new AuthScope(host, this.getRealm(), this.getName());
        Credentials credentials = credentialsProvider.getCredentials(authScope, context);
        if (credentials != null) {
            this.username = credentials.getUserPrincipal().getName();
            this.password = credentials.getPassword();
            return true;
        }
        LOG.debug("No credentials found for auth scope [{}]", (Object)authScope);
        this.username = null;
        this.password = null;
        return false;
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public String generateAuthResponse(HttpHost host, HttpRequest request, HttpContext context) throws AuthenticationException {
        Args.notNull(request, "HTTP request");
        if (this.paramMap.get("realm") == null) {
            throw new AuthenticationException("missing realm");
        }
        if (this.paramMap.get("nonce") == null) {
            throw new AuthenticationException("missing nonce");
        }
        return this.createDigestResponse(request);
    }

    private static MessageDigest createMessageDigest(String digAlg) throws UnsupportedDigestAlgorithmException {
        try {
            return MessageDigest.getInstance(digAlg);
        } catch (Exception e) {
            throw new UnsupportedDigestAlgorithmException("Unsupported algorithm in HTTP Digest authentication: " + digAlg);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private String createDigestResponse(HttpRequest request) throws AuthenticationException {
        byte[] digestInput;
        MessageDigest digester;
        Charset charset;
        String uri = request.getRequestUri();
        String method = request.getMethod();
        String realm = this.paramMap.get("realm");
        String nonce = this.paramMap.get("nonce");
        String opaque = this.paramMap.get("opaque");
        String algorithm = this.paramMap.get("algorithm");
        if (algorithm == null) {
            algorithm = "MD5";
        }
        HashSet<String> qopset = new HashSet<String>(8);
        int qop = -1;
        String qoplist = this.paramMap.get("qop");
        if (qoplist != null) {
            HttpEntity entity;
            StringTokenizer tok = new StringTokenizer(qoplist, ",");
            while (tok.hasMoreTokens()) {
                String variant = tok.nextToken().trim();
                qopset.add(variant.toLowerCase(Locale.ROOT));
            }
            HttpEntity httpEntity = entity = request instanceof ClassicHttpRequest ? ((ClassicHttpRequest)request).getEntity() : null;
            if (entity != null && qopset.contains("auth-int")) {
                qop = 1;
            } else if (qopset.contains("auth")) {
                qop = 2;
            } else if (qopset.contains("auth-int")) {
                qop = 1;
            }
        } else {
            qop = 0;
        }
        if (qop == -1) {
            throw new AuthenticationException("None of the qop methods is supported: " + qoplist);
        }
        String charsetName = this.paramMap.get("charset");
        try {
            charset = charsetName != null ? Charset.forName(charsetName) : StandardCharsets.ISO_8859_1;
        } catch (UnsupportedCharsetException ex) {
            charset = StandardCharsets.ISO_8859_1;
        }
        String digAlg = algorithm;
        if (digAlg.equalsIgnoreCase("MD5-sess")) {
            digAlg = "MD5";
        }
        try {
            digester = DigestScheme.createMessageDigest(digAlg);
        } catch (UnsupportedDigestAlgorithmException ex) {
            throw new AuthenticationException("Unsuppported digest algorithm: " + digAlg);
        }
        if (nonce.equals(this.lastNonce)) {
            ++this.nounceCount;
        } else {
            this.nounceCount = 1L;
            this.cnonce = null;
            this.lastNonce = nonce;
        }
        StringBuilder sb = new StringBuilder(8);
        try (Formatter formatter = new Formatter(sb, Locale.ROOT);){
            formatter.format("%08x", this.nounceCount);
        }
        String nc = sb.toString();
        if (this.cnonce == null) {
            this.cnonce = DigestScheme.formatHex(DigestScheme.createCnonce());
        }
        if (this.buffer == null) {
            this.buffer = new ByteArrayBuilder(128);
        } else {
            this.buffer.reset();
        }
        this.buffer.charset(charset);
        this.a1 = null;
        this.a2 = null;
        if (algorithm.equalsIgnoreCase("MD5-sess")) {
            this.buffer.append(this.username).append(":").append(realm).append(":").append(this.password);
            String checksum = DigestScheme.formatHex(digester.digest(this.buffer.toByteArray()));
            this.buffer.reset();
            this.buffer.append(checksum).append(":").append(nonce).append(":").append(this.cnonce);
            this.a1 = this.buffer.toByteArray();
        } else {
            this.buffer.append(this.username).append(":").append(realm).append(":").append(this.password);
            this.a1 = this.buffer.toByteArray();
        }
        String hasha1 = DigestScheme.formatHex(digester.digest(this.a1));
        this.buffer.reset();
        if (qop == 2) {
            this.a2 = this.buffer.append(method).append(":").append(uri).toByteArray();
        } else if (qop == 1) {
            HttpEntity entity;
            HttpEntity httpEntity = entity = request instanceof ClassicHttpRequest ? ((ClassicHttpRequest)request).getEntity() : null;
            if (entity != null && !entity.isRepeatable()) {
                if (!qopset.contains("auth")) throw new AuthenticationException("Qop auth-int cannot be used with a non-repeatable entity");
                qop = 2;
                this.a2 = this.buffer.append(method).append(":").append(uri).toByteArray();
            } else {
                HttpEntityDigester entityDigester = new HttpEntityDigester(digester);
                try {
                    if (entity != null) {
                        entity.writeTo(entityDigester);
                    }
                    entityDigester.close();
                } catch (IOException ex) {
                    throw new AuthenticationException("I/O error reading entity content", ex);
                }
                this.a2 = this.buffer.append(method).append(":").append(uri).append(":").append(DigestScheme.formatHex(entityDigester.getDigest())).toByteArray();
            }
        } else {
            this.a2 = this.buffer.append(method).append(":").append(uri).toByteArray();
        }
        String hasha2 = DigestScheme.formatHex(digester.digest(this.a2));
        this.buffer.reset();
        if (qop == 0) {
            this.buffer.append(hasha1).append(":").append(nonce).append(":").append(hasha2);
            digestInput = this.buffer.toByteArray();
        } else {
            this.buffer.append(hasha1).append(":").append(nonce).append(":").append(nc).append(":").append(this.cnonce).append(":").append(qop == 1 ? "auth-int" : "auth").append(":").append(hasha2);
            digestInput = this.buffer.toByteArray();
        }
        this.buffer.reset();
        String digest = DigestScheme.formatHex(digester.digest(digestInput));
        CharArrayBuffer buffer = new CharArrayBuffer(128);
        buffer.append("Digest ");
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(20);
        params.add(new BasicNameValuePair("username", this.username));
        params.add(new BasicNameValuePair("realm", realm));
        params.add(new BasicNameValuePair("nonce", nonce));
        params.add(new BasicNameValuePair("uri", uri));
        params.add(new BasicNameValuePair("response", digest));
        if (qop != 0) {
            params.add(new BasicNameValuePair("qop", qop == 1 ? "auth-int" : "auth"));
            params.add(new BasicNameValuePair("nc", nc));
            params.add(new BasicNameValuePair("cnonce", this.cnonce));
        }
        params.add(new BasicNameValuePair("algorithm", algorithm));
        if (opaque != null) {
            params.add(new BasicNameValuePair("opaque", opaque));
        }
        for (int i = 0; i < params.size(); ++i) {
            String name;
            BasicNameValuePair param = (BasicNameValuePair)params.get(i);
            if (i > 0) {
                buffer.append(", ");
            }
            boolean noQuotes = "nc".equals(name = param.getName()) || "qop".equals(name) || "algorithm".equals(name);
            BasicHeaderValueFormatter.INSTANCE.formatNameValuePair(buffer, param, !noQuotes);
        }
        return buffer.toString();
    }

    @Internal
    public String getNonce() {
        return this.lastNonce;
    }

    @Internal
    public long getNounceCount() {
        return this.nounceCount;
    }

    @Internal
    public String getCnonce() {
        return this.cnonce;
    }

    String getA1() {
        return this.a1 != null ? new String(this.a1, StandardCharsets.US_ASCII) : null;
    }

    String getA2() {
        return this.a2 != null ? new String(this.a2, StandardCharsets.US_ASCII) : null;
    }

    static String formatHex(byte[] binaryData) {
        int n = binaryData.length;
        char[] buffer = new char[n * 2];
        for (int i = 0; i < n; ++i) {
            int low = binaryData[i] & 0xF;
            int high = (binaryData[i] & 0xF0) >> 4;
            buffer[i * 2] = HEXADECIMAL[high];
            buffer[i * 2 + 1] = HEXADECIMAL[low];
        }
        return new String(buffer);
    }

    static byte[] createCnonce() {
        SecureRandom rnd = new SecureRandom();
        byte[] tmp = new byte[8];
        rnd.nextBytes(tmp);
        return tmp;
    }

    public String toString() {
        return this.getName() + this.paramMap;
    }
}

