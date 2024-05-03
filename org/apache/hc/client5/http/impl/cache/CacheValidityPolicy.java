/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Date;
import java.util.Iterator;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.util.TimeValue;

class CacheValidityPolicy {
    public static final TimeValue MAX_AGE = TimeValue.ofSeconds(0x80000000L);

    CacheValidityPolicy() {
    }

    public TimeValue getCurrentAge(HttpCacheEntry entry, Date now) {
        return TimeValue.ofSeconds(this.getCorrectedInitialAge(entry).toSeconds() + this.getResidentTime(entry, now).toSeconds());
    }

    public TimeValue getFreshnessLifetime(HttpCacheEntry entry) {
        long maxAge = this.getMaxAge(entry);
        if (maxAge > -1L) {
            return TimeValue.ofSeconds(maxAge);
        }
        Date dateValue = entry.getDate();
        if (dateValue == null) {
            return TimeValue.ZERO_MILLISECONDS;
        }
        Date expiry = DateUtils.parseDate(entry, "Expires");
        if (expiry == null) {
            return TimeValue.ZERO_MILLISECONDS;
        }
        long diff = expiry.getTime() - dateValue.getTime();
        return TimeValue.ofSeconds(diff / 1000L);
    }

    public boolean isResponseFresh(HttpCacheEntry entry, Date now) {
        return this.getCurrentAge(entry, now).compareTo(this.getFreshnessLifetime(entry)) == -1;
    }

    public boolean isResponseHeuristicallyFresh(HttpCacheEntry entry, Date now, float coefficient, TimeValue defaultLifetime) {
        return this.getCurrentAge(entry, now).compareTo(this.getHeuristicFreshnessLifetime(entry, coefficient, defaultLifetime)) == -1;
    }

    public TimeValue getHeuristicFreshnessLifetime(HttpCacheEntry entry, float coefficient, TimeValue defaultLifetime) {
        Date dateValue = entry.getDate();
        Date lastModifiedValue = DateUtils.parseDate(entry, "Last-Modified");
        if (dateValue != null && lastModifiedValue != null) {
            long diff = dateValue.getTime() - lastModifiedValue.getTime();
            if (diff < 0L) {
                return TimeValue.ZERO_MILLISECONDS;
            }
            return TimeValue.ofSeconds((long)(coefficient * (float)diff / 1000.0f));
        }
        return defaultLifetime;
    }

    public boolean isRevalidatable(HttpCacheEntry entry) {
        return entry.getFirstHeader("ETag") != null || entry.getFirstHeader("Last-Modified") != null;
    }

    public boolean mustRevalidate(HttpCacheEntry entry) {
        return this.hasCacheControlDirective(entry, "must-revalidate");
    }

    public boolean proxyRevalidate(HttpCacheEntry entry) {
        return this.hasCacheControlDirective(entry, "proxy-revalidate");
    }

    public boolean mayReturnStaleWhileRevalidating(HttpCacheEntry entry, Date now) {
        Iterator<HeaderElement> it = MessageSupport.iterate(entry, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if (!"stale-while-revalidate".equalsIgnoreCase(elt.getName())) continue;
            try {
                int allowedStalenessLifetime = Integer.parseInt(elt.getValue());
                if (this.getStaleness(entry, now).compareTo(TimeValue.ofSeconds(allowedStalenessLifetime)) > 0) continue;
                return true;
            } catch (NumberFormatException nfe) {
            }
        }
        return false;
    }

    public boolean mayReturnStaleIfError(HttpRequest request, HttpCacheEntry entry, Date now) {
        TimeValue staleness = this.getStaleness(entry, now);
        return this.mayReturnStaleIfError((MessageHeaders)request, "Cache-Control", staleness) || this.mayReturnStaleIfError(entry, "Cache-Control", staleness);
    }

    private boolean mayReturnStaleIfError(MessageHeaders headers, String name, TimeValue staleness) {
        boolean result = false;
        Iterator<HeaderElement> it = MessageSupport.iterate(headers, name);
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if (!"stale-if-error".equals(elt.getName())) continue;
            try {
                int staleIfError = Integer.parseInt(elt.getValue());
                if (staleness.compareTo(TimeValue.ofSeconds(staleIfError)) > 0) continue;
                result = true;
                break;
            } catch (NumberFormatException nfe) {
            }
        }
        return result;
    }

    protected boolean contentLengthHeaderMatchesActualLength(HttpCacheEntry entry) {
        Header h = entry.getFirstHeader("Content-Length");
        if (h != null) {
            try {
                long responseLen = Long.parseLong(h.getValue());
                Resource resource = entry.getResource();
                if (resource == null) {
                    return false;
                }
                long resourceLen = resource.length();
                return responseLen == resourceLen;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }

    protected TimeValue getApparentAge(HttpCacheEntry entry) {
        Date dateValue = entry.getDate();
        if (dateValue == null) {
            return MAX_AGE;
        }
        long diff = entry.getResponseDate().getTime() - dateValue.getTime();
        if (diff < 0L) {
            return TimeValue.ZERO_MILLISECONDS;
        }
        return TimeValue.ofSeconds(diff / 1000L);
    }

    protected long getAgeValue(HttpCacheEntry entry) {
        long ageValue = 0L;
        for (Header hdr : entry.getHeaders("Age")) {
            long hdrAge;
            try {
                hdrAge = Long.parseLong(hdr.getValue());
                if (hdrAge < 0L) {
                    hdrAge = MAX_AGE.toSeconds();
                }
            } catch (NumberFormatException nfe) {
                hdrAge = MAX_AGE.toSeconds();
            }
            ageValue = hdrAge > ageValue ? hdrAge : ageValue;
        }
        return ageValue;
    }

    protected TimeValue getCorrectedReceivedAge(HttpCacheEntry entry) {
        TimeValue apparentAge = this.getApparentAge(entry);
        long ageValue = this.getAgeValue(entry);
        return apparentAge.toSeconds() > ageValue ? apparentAge : TimeValue.ofSeconds(ageValue);
    }

    protected TimeValue getResponseDelay(HttpCacheEntry entry) {
        long diff = entry.getResponseDate().getTime() - entry.getRequestDate().getTime();
        return TimeValue.ofSeconds(diff / 1000L);
    }

    protected TimeValue getCorrectedInitialAge(HttpCacheEntry entry) {
        return TimeValue.ofSeconds(this.getCorrectedReceivedAge(entry).toSeconds() + this.getResponseDelay(entry).toSeconds());
    }

    protected TimeValue getResidentTime(HttpCacheEntry entry, Date now) {
        long diff = now.getTime() - entry.getResponseDate().getTime();
        return TimeValue.ofSeconds(diff / 1000L);
    }

    protected long getMaxAge(HttpCacheEntry entry) {
        long maxAge = -1L;
        Iterator<HeaderElement> it = MessageSupport.iterate(entry, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if (!"max-age".equals(elt.getName()) && !"s-maxage".equals(elt.getName())) continue;
            try {
                long currMaxAge = Long.parseLong(elt.getValue());
                if (maxAge != -1L && currMaxAge >= maxAge) continue;
                maxAge = currMaxAge;
            } catch (NumberFormatException nfe) {
                maxAge = 0L;
            }
        }
        return maxAge;
    }

    public boolean hasCacheControlDirective(HttpCacheEntry entry, String directive) {
        Iterator<HeaderElement> it = MessageSupport.iterate(entry, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if (!directive.equalsIgnoreCase(elt.getName())) continue;
            return true;
        }
        return false;
    }

    public TimeValue getStaleness(HttpCacheEntry entry, Date now) {
        TimeValue freshness;
        TimeValue age = this.getCurrentAge(entry, now);
        if (age.compareTo(freshness = this.getFreshnessLifetime(entry)) <= 0) {
            return TimeValue.ZERO_MILLISECONDS;
        }
        return TimeValue.ofSeconds(age.toSeconds() - freshness.toSeconds());
    }
}

