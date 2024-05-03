/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.net.UrlConnectionFactory;
import org.apache.logging.log4j.core.net.ssl.SslConfigurationFactory;
import org.apache.logging.log4j.core.util.AuthorizationProvider;
import org.apache.logging.log4j.core.util.internal.LastModifiedSource;
import org.apache.logging.log4j.core.util.internal.Status;
import org.apache.logging.log4j.status.StatusLogger;

public final class HttpInputStreamUtil {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final int NOT_MODIFIED = 304;
    private static final int NOT_AUTHORIZED = 401;
    private static final int NOT_FOUND = 404;
    private static final int OK = 200;
    private static final int BUF_SIZE = 1024;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Result getInputStream(LastModifiedSource source, AuthorizationProvider authorizationProvider) {
        Result result = new Result();
        try {
            long lastModified = source.getLastModified();
            HttpURLConnection connection = (HttpURLConnection)UrlConnectionFactory.createConnection(source.getURI().toURL(), lastModified, SslConfigurationFactory.getSslConfiguration(), authorizationProvider);
            connection.connect();
            try {
                int code = connection.getResponseCode();
                switch (code) {
                    case 304: {
                        LOGGER.debug("Configuration not modified");
                        result.status = Status.NOT_MODIFIED;
                        Result result2 = result;
                        return result2;
                    }
                    case 404: {
                        LOGGER.debug("Unable to access {}: Not Found", (Object)source.toString());
                        result.status = Status.NOT_FOUND;
                        Result result3 = result;
                        return result3;
                    }
                    case 200: {
                        Throwable throwable = null;
                        try (InputStream is = connection.getInputStream();){
                            source.setLastModified(connection.getLastModified());
                            LOGGER.debug("Content was modified for {}. previous lastModified: {}, new lastModified: {}", (Object)source.toString(), (Object)lastModified, (Object)connection.getLastModified());
                            result.status = Status.SUCCESS;
                            result.inputStream = new ByteArrayInputStream(HttpInputStreamUtil.readStream(is));
                            Result result4 = result;
                            return result4;
                        } catch (Throwable throwable2) {
                            try {
                                throwable = throwable2;
                                throw throwable2;
                            } catch (IOException e) {
                                try (InputStream es = connection.getErrorStream();){
                                    LOGGER.info("Error accessing configuration at {}: {}", (Object)source.toString(), (Object)HttpInputStreamUtil.readStream(es));
                                    throw new ConfigurationException("Unable to access " + source.toString(), e);
                                } catch (IOException ioe) {
                                    LOGGER.error("Error accessing configuration at {}: {}", (Object)source.toString(), (Object)e.getMessage());
                                }
                                throw new ConfigurationException("Unable to access " + source.toString(), e);
                            }
                        }
                    }
                    case 401: {
                        throw new ConfigurationException("Authorization failed");
                    }
                }
                if (code < 0) {
                    LOGGER.info("Invalid response code returned");
                    throw new ConfigurationException("Unable to access " + source.toString());
                }
                LOGGER.info("Unexpected response code returned {}", (Object)code);
                throw new ConfigurationException("Unable to access " + source.toString());
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            LOGGER.warn("Error accessing {}: {}", (Object)source.toString(), (Object)e.getMessage());
            throw new ConfigurationException("Unable to access " + source.toString(), e);
        }
    }

    public static byte[] readStream(InputStream is) throws IOException {
        int length;
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toByteArray();
    }

    public static class Result {
        private InputStream inputStream;
        private Status status;

        public Result() {
        }

        public Result(Status status) {
            this.status = status;
        }

        public InputStream getInputStream() {
            return this.inputStream;
        }

        public Status getStatus() {
            return this.status;
        }
    }
}

