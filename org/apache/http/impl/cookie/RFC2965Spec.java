/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.cookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.impl.cookie.RFC2109Spec;
import org.apache.http.impl.cookie.RFC2965CommentUrlAttributeHandler;
import org.apache.http.impl.cookie.RFC2965DiscardAttributeHandler;
import org.apache.http.impl.cookie.RFC2965DomainAttributeHandler;
import org.apache.http.impl.cookie.RFC2965PortAttributeHandler;
import org.apache.http.impl.cookie.RFC2965VersionAttributeHandler;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@NotThreadSafe
public class RFC2965Spec
extends RFC2109Spec {
    public RFC2965Spec() {
        this(null, false);
    }

    public RFC2965Spec(String[] datepatterns, boolean oneHeader) {
        super(datepatterns, oneHeader);
        this.registerAttribHandler("domain", new RFC2965DomainAttributeHandler());
        this.registerAttribHandler("port", new RFC2965PortAttributeHandler());
        this.registerAttribHandler("commenturl", new RFC2965CommentUrlAttributeHandler());
        this.registerAttribHandler("discard", new RFC2965DiscardAttributeHandler());
        this.registerAttribHandler("version", new RFC2965VersionAttributeHandler());
    }

    @Override
    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(header, "Header");
        Args.notNull(origin, "Cookie origin");
        if (!header.getName().equalsIgnoreCase("Set-Cookie2")) {
            throw new MalformedCookieException("Unrecognized cookie header '" + header.toString() + "'");
        }
        HeaderElement[] elems = header.getElements();
        return this.createCookies(elems, RFC2965Spec.adjustEffectiveHost(origin));
    }

    @Override
    protected List<Cookie> parse(HeaderElement[] elems, CookieOrigin origin) throws MalformedCookieException {
        return this.createCookies(elems, RFC2965Spec.adjustEffectiveHost(origin));
    }

    private List<Cookie> createCookies(HeaderElement[] elems, CookieOrigin origin) throws MalformedCookieException {
        ArrayList<Cookie> cookies = new ArrayList<Cookie>(elems.length);
        for (HeaderElement headerelement : elems) {
            String name = headerelement.getName();
            String value = headerelement.getValue();
            if (name == null || name.length() == 0) {
                throw new MalformedCookieException("Cookie name may not be empty");
            }
            BasicClientCookie2 cookie = new BasicClientCookie2(name, value);
            cookie.setPath(RFC2965Spec.getDefaultPath(origin));
            cookie.setDomain(RFC2965Spec.getDefaultDomain(origin));
            cookie.setPorts(new int[]{origin.getPort()});
            NameValuePair[] attribs = headerelement.getParameters();
            HashMap<String, NameValuePair> attribmap = new HashMap<String, NameValuePair>(attribs.length);
            for (int j = attribs.length - 1; j >= 0; --j) {
                NameValuePair param = attribs[j];
                attribmap.put(param.getName().toLowerCase(Locale.ENGLISH), param);
            }
            for (Map.Entry entry : attribmap.entrySet()) {
                NameValuePair attrib = (NameValuePair)entry.getValue();
                String s = attrib.getName().toLowerCase(Locale.ENGLISH);
                cookie.setAttribute(s, attrib.getValue());
                CookieAttributeHandler handler = this.findAttribHandler(s);
                if (handler == null) continue;
                handler.parse(cookie, attrib.getValue());
            }
            cookies.add(cookie);
        }
        return cookies;
    }

    @Override
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        super.validate(cookie, RFC2965Spec.adjustEffectiveHost(origin));
    }

    @Override
    public boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        return super.match(cookie, RFC2965Spec.adjustEffectiveHost(origin));
    }

    @Override
    protected void formatCookieAsVer(CharArrayBuffer buffer, Cookie cookie, int version) {
        String s;
        super.formatCookieAsVer(buffer, cookie, version);
        if (cookie instanceof ClientCookie && (s = ((ClientCookie)cookie).getAttribute("port")) != null) {
            int[] ports;
            buffer.append("; $Port");
            buffer.append("=\"");
            if (s.trim().length() > 0 && (ports = cookie.getPorts()) != null) {
                int len = ports.length;
                for (int i = 0; i < len; ++i) {
                    if (i > 0) {
                        buffer.append(",");
                    }
                    buffer.append(Integer.toString(ports[i]));
                }
            }
            buffer.append("\"");
        }
    }

    private static CookieOrigin adjustEffectiveHost(CookieOrigin origin) {
        String host = origin.getHost();
        boolean isLocalHost = true;
        for (int i = 0; i < host.length(); ++i) {
            char ch = host.charAt(i);
            if (ch != '.' && ch != ':') continue;
            isLocalHost = false;
            break;
        }
        if (isLocalHost) {
            host = host + ".local";
            return new CookieOrigin(host, origin.getPort(), origin.getPath(), origin.isSecure());
        }
        return origin;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public Header getVersionHeader() {
        CharArrayBuffer buffer = new CharArrayBuffer(40);
        buffer.append("Cookie2");
        buffer.append(": ");
        buffer.append("$Version=");
        buffer.append(Integer.toString(this.getVersion()));
        return new BufferedHeader(buffer);
    }

    @Override
    public String toString() {
        return "rfc2965";
    }
}

