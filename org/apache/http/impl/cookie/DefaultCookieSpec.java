/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.cookie;

import java.util.List;
import org.apache.http.FormattedHeader;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.cookie.CommonCookieAttributeHandler;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie2;
import org.apache.http.impl.cookie.BasicCommentHandler;
import org.apache.http.impl.cookie.BasicDomainHandler;
import org.apache.http.impl.cookie.BasicExpiresHandler;
import org.apache.http.impl.cookie.BasicMaxAgeHandler;
import org.apache.http.impl.cookie.BasicPathHandler;
import org.apache.http.impl.cookie.BasicSecureHandler;
import org.apache.http.impl.cookie.NetscapeDraftHeaderParser;
import org.apache.http.impl.cookie.NetscapeDraftSpec;
import org.apache.http.impl.cookie.RFC2109DomainHandler;
import org.apache.http.impl.cookie.RFC2109Spec;
import org.apache.http.impl.cookie.RFC2109VersionHandler;
import org.apache.http.impl.cookie.RFC2965CommentUrlAttributeHandler;
import org.apache.http.impl.cookie.RFC2965DiscardAttributeHandler;
import org.apache.http.impl.cookie.RFC2965DomainAttributeHandler;
import org.apache.http.impl.cookie.RFC2965PortAttributeHandler;
import org.apache.http.impl.cookie.RFC2965Spec;
import org.apache.http.impl.cookie.RFC2965VersionAttributeHandler;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

@Contract(threading=ThreadingBehavior.SAFE)
public class DefaultCookieSpec
implements CookieSpec {
    private final RFC2965Spec strict;
    private final RFC2109Spec obsoleteStrict;
    private final NetscapeDraftSpec netscapeDraft;

    DefaultCookieSpec(RFC2965Spec strict, RFC2109Spec obsoleteStrict, NetscapeDraftSpec netscapeDraft) {
        this.strict = strict;
        this.obsoleteStrict = obsoleteStrict;
        this.netscapeDraft = netscapeDraft;
    }

    public DefaultCookieSpec(String[] datepatterns, boolean oneHeader) {
        String[] stringArray;
        this.strict = new RFC2965Spec(oneHeader, new CommonCookieAttributeHandler[]{new RFC2965VersionAttributeHandler(), new BasicPathHandler(), new RFC2965DomainAttributeHandler(), new RFC2965PortAttributeHandler(), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicCommentHandler(), new RFC2965CommentUrlAttributeHandler(), new RFC2965DiscardAttributeHandler()});
        this.obsoleteStrict = new RFC2109Spec(oneHeader, new CommonCookieAttributeHandler[]{new RFC2109VersionHandler(), new BasicPathHandler(), new RFC2109DomainHandler(), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicCommentHandler()});
        CommonCookieAttributeHandler[] commonCookieAttributeHandlerArray = new CommonCookieAttributeHandler[5];
        commonCookieAttributeHandlerArray[0] = new BasicDomainHandler();
        commonCookieAttributeHandlerArray[1] = new BasicPathHandler();
        commonCookieAttributeHandlerArray[2] = new BasicSecureHandler();
        commonCookieAttributeHandlerArray[3] = new BasicCommentHandler();
        if (datepatterns != null) {
            stringArray = (String[])datepatterns.clone();
        } else {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = "EEE, dd-MMM-yy HH:mm:ss z";
        }
        commonCookieAttributeHandlerArray[4] = new BasicExpiresHandler(stringArray);
        this.netscapeDraft = new NetscapeDraftSpec(commonCookieAttributeHandlerArray);
    }

    public DefaultCookieSpec() {
        this(null, false);
    }

    @Override
    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(header, "Header");
        Args.notNull(origin, "Cookie origin");
        HeaderElement[] hElems = header.getElements();
        boolean versioned = false;
        boolean netscape = false;
        for (HeaderElement hElem : hElems) {
            if (hElem.getParameterByName("version") != null) {
                versioned = true;
            }
            if (hElem.getParameterByName("expires") == null) continue;
            netscape = true;
        }
        if (netscape || !versioned) {
            ParserCursor cursor;
            CharArrayBuffer buffer;
            NetscapeDraftHeaderParser parser = NetscapeDraftHeaderParser.DEFAULT;
            if (header instanceof FormattedHeader) {
                buffer = ((FormattedHeader)header).getBuffer();
                cursor = new ParserCursor(((FormattedHeader)header).getValuePos(), buffer.length());
            } else {
                String hValue = header.getValue();
                if (hValue == null) {
                    throw new MalformedCookieException("Header value is null");
                }
                buffer = new CharArrayBuffer(hValue.length());
                buffer.append(hValue);
                cursor = new ParserCursor(0, buffer.length());
            }
            hElems = new HeaderElement[]{parser.parseHeader(buffer, cursor)};
            return this.netscapeDraft.parse(hElems, origin);
        }
        return "Set-Cookie2".equals(header.getName()) ? this.strict.parse(hElems, origin) : this.obsoleteStrict.parse(hElems, origin);
    }

    @Override
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        if (cookie.getVersion() > 0) {
            if (cookie instanceof SetCookie2) {
                this.strict.validate(cookie, origin);
            } else {
                this.obsoleteStrict.validate(cookie, origin);
            }
        } else {
            this.netscapeDraft.validate(cookie, origin);
        }
    }

    @Override
    public boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        if (cookie.getVersion() > 0) {
            return cookie instanceof SetCookie2 ? this.strict.match(cookie, origin) : this.obsoleteStrict.match(cookie, origin);
        }
        return this.netscapeDraft.match(cookie, origin);
    }

    @Override
    public List<Header> formatCookies(List<Cookie> cookies) {
        Args.notNull(cookies, "List of cookies");
        int version = Integer.MAX_VALUE;
        boolean isSetCookie2 = true;
        for (Cookie cookie : cookies) {
            if (!(cookie instanceof SetCookie2)) {
                isSetCookie2 = false;
            }
            if (cookie.getVersion() >= version) continue;
            version = cookie.getVersion();
        }
        if (version > 0) {
            return isSetCookie2 ? this.strict.formatCookies(cookies) : this.obsoleteStrict.formatCookies(cookies);
        }
        return this.netscapeDraft.formatCookies(cookies);
    }

    @Override
    public int getVersion() {
        return this.strict.getVersion();
    }

    @Override
    public Header getVersionHeader() {
        return null;
    }

    public String toString() {
        return "default";
    }
}

