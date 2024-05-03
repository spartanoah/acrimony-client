/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.client;

import com.google.gson.JsonParser;
import com.mojang.realmsclient.RealmsVersion;
import com.mojang.realmsclient.client.UploadStatus;
import com.mojang.realmsclient.dto.UploadInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUpload {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String UPLOAD_PATH = "/upload";
    private volatile boolean cancelled = false;
    private volatile boolean finished = false;
    private HttpPost request;
    private int statusCode = -1;
    private String errorMessage;
    private RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout((int)TimeUnit.MINUTES.toMillis(10L)).setConnectTimeout((int)TimeUnit.SECONDS.toMillis(15L)).build();
    private Thread currentThread;

    public void upload(final File file, final long worldId, final int slotId, final UploadInfo uploadInfo, final String sessionId, final String username, final String clientVersion, final UploadStatus uploadStatus) {
        if (this.currentThread != null) {
            return;
        }
        this.currentThread = new Thread(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                FileUpload.this.request = new HttpPost("http://" + uploadInfo.getUploadEndpoint() + ":" + String.valueOf(uploadInfo.getPort()) + FileUpload.UPLOAD_PATH + "/" + String.valueOf(worldId) + "/" + String.valueOf(slotId));
                CloseableHttpClient client = null;
                try {
                    String json;
                    client = HttpClientBuilder.create().setDefaultRequestConfig(FileUpload.this.requestConfig).build();
                    String realmsVersion = RealmsVersion.getVersion();
                    if (realmsVersion != null) {
                        FileUpload.this.request.setHeader("Cookie", "sid=" + sessionId + ";token=" + uploadInfo.getToken() + ";user=" + username + ";version=" + clientVersion + ";realms_version=" + realmsVersion);
                    } else {
                        FileUpload.this.request.setHeader("Cookie", "sid=" + sessionId + ";token=" + uploadInfo.getToken() + ";user=" + username + ";version=" + clientVersion);
                    }
                    uploadStatus.totalBytes = file.length();
                    CustomInputStreamEntity entity = new CustomInputStreamEntity((InputStream)new FileInputStream(file), file.length(), uploadStatus);
                    entity.setContentType("application/octet-stream");
                    FileUpload.this.request.setEntity(entity);
                    CloseableHttpResponse response = client.execute(FileUpload.this.request);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 401) {
                        LOGGER.debug("Realms server returned 401: " + response.getFirstHeader("WWW-Authenticate"));
                    }
                    FileUpload.this.statusCode = statusCode;
                    if (response.getEntity() != null && (json = EntityUtils.toString(response.getEntity(), "UTF-8")) != null) {
                        try {
                            JsonParser parser = new JsonParser();
                            FileUpload.this.errorMessage = parser.parse(json).getAsJsonObject().get("errorMsg").getAsString();
                        } catch (Exception exception) {
                            // empty catch block
                        }
                    }
                } catch (Exception e) {
                    if (!FileUpload.this.cancelled) {
                        LOGGER.error("Caught exception while uploading: ", (Throwable)e);
                    }
                } finally {
                    FileUpload.this.request.releaseConnection();
                    FileUpload.this.finished = true;
                    if (client != null) {
                        try {
                            client.close();
                        } catch (IOException e) {
                            LOGGER.error("Failed to close Realms upload client");
                        }
                    }
                }
            }
        };
        this.currentThread.start();
    }

    public void cancel() {
        this.cancelled = true;
        if (this.request != null) {
            this.request.abort();
        }
    }

    public boolean isFinished() {
        return this.finished;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    private static class CustomInputStreamEntity
    extends InputStreamEntity {
        private final long length;
        private final InputStream content;
        private final UploadStatus uploadStatus;

        public CustomInputStreamEntity(InputStream instream, long length, UploadStatus uploadStatus) {
            super(instream);
            this.content = instream;
            this.length = length;
            this.uploadStatus = uploadStatus;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void writeTo(OutputStream outstream) throws IOException {
            block7: {
                Args.notNull(outstream, "Output stream");
                InputStream instream = this.content;
                try {
                    int l;
                    byte[] buffer = new byte[4096];
                    if (this.length < 0L) {
                        int l2;
                        while ((l2 = instream.read(buffer)) != -1) {
                            outstream.write(buffer, 0, l2);
                            UploadStatus uploadStatus = this.uploadStatus;
                            Long.valueOf(uploadStatus.bytesWritten + (long)l2);
                            uploadStatus.bytesWritten = uploadStatus.bytesWritten;
                        }
                        break block7;
                    }
                    for (long remaining = this.length; remaining > 0L; remaining -= (long)l) {
                        l = instream.read(buffer, 0, (int)Math.min(4096L, remaining));
                        if (l == -1) {
                            break;
                        }
                        outstream.write(buffer, 0, l);
                        UploadStatus uploadStatus = this.uploadStatus;
                        Long.valueOf(uploadStatus.bytesWritten + (long)l);
                        uploadStatus.bytesWritten = uploadStatus.bytesWritten;
                        outstream.flush();
                    }
                } finally {
                    instream.close();
                }
            }
        }
    }
}

