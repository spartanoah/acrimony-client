/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

public final class HttpStatus {
    public static final int SC_INFORMATIONAL = 100;
    public static final int SC_CONTINUE = 100;
    public static final int SC_SWITCHING_PROTOCOLS = 101;
    public static final int SC_PROCESSING = 102;
    public static final int SC_EARLY_HINTS = 103;
    public static final int SC_SUCCESS = 200;
    public static final int SC_OK = 200;
    public static final int SC_CREATED = 201;
    public static final int SC_ACCEPTED = 202;
    public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int SC_NO_CONTENT = 204;
    public static final int SC_RESET_CONTENT = 205;
    public static final int SC_PARTIAL_CONTENT = 206;
    public static final int SC_MULTI_STATUS = 207;
    public static final int SC_ALREADY_REPORTED = 208;
    public static final int SC_IM_USED = 226;
    public static final int SC_REDIRECTION = 300;
    public static final int SC_MULTIPLE_CHOICES = 300;
    public static final int SC_MOVED_PERMANENTLY = 301;
    public static final int SC_MOVED_TEMPORARILY = 302;
    public static final int SC_SEE_OTHER = 303;
    public static final int SC_NOT_MODIFIED = 304;
    public static final int SC_USE_PROXY = 305;
    public static final int SC_TEMPORARY_REDIRECT = 307;
    public static final int SC_PERMANENT_REDIRECT = 308;
    public static final int SC_CLIENT_ERROR = 400;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_PAYMENT_REQUIRED = 402;
    public static final int SC_FORBIDDEN = 403;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_METHOD_NOT_ALLOWED = 405;
    public static final int SC_NOT_ACCEPTABLE = 406;
    public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int SC_REQUEST_TIMEOUT = 408;
    public static final int SC_CONFLICT = 409;
    public static final int SC_GONE = 410;
    public static final int SC_LENGTH_REQUIRED = 411;
    public static final int SC_PRECONDITION_FAILED = 412;
    public static final int SC_REQUEST_TOO_LONG = 413;
    public static final int SC_REQUEST_URI_TOO_LONG = 414;
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    public static final int SC_EXPECTATION_FAILED = 417;
    public static final int SC_MISDIRECTED_REQUEST = 421;
    public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
    public static final int SC_METHOD_FAILURE = 420;
    public static final int SC_UNPROCESSABLE_ENTITY = 422;
    public static final int SC_LOCKED = 423;
    public static final int SC_FAILED_DEPENDENCY = 424;
    public static final int SC_PRECONDITION_REQUIRED = 428;
    public static final int SC_TOO_MANY_REQUESTS = 429;
    public static final int SC_REQUEST_HEADER_FIELDS_TOO_LARGE = 431;
    public static final int SC_UNAVAILABLE_FOR_LEGAL_REASONS = 451;
    public static final int SC_SERVER_ERROR = 500;
    public static final int SC_INTERNAL_SERVER_ERROR = 500;
    public static final int SC_NOT_IMPLEMENTED = 501;
    public static final int SC_BAD_GATEWAY = 502;
    public static final int SC_SERVICE_UNAVAILABLE = 503;
    public static final int SC_GATEWAY_TIMEOUT = 504;
    public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
    public static final int SC_VARIANT_ALSO_NEGOTIATES = 506;
    public static final int SC_INSUFFICIENT_STORAGE = 507;
    public static final int SC_LOOP_DETECTED = 508;
    public static final int SC_NOT_EXTENDED = 510;
    public static final int SC_NETWORK_AUTHENTICATION_REQUIRED = 511;

    private HttpStatus() {
    }
}

