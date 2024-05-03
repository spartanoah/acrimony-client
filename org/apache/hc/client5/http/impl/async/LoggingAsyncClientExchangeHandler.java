/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Identifiable;
import org.slf4j.Logger;

final class LoggingAsyncClientExchangeHandler
implements AsyncClientExchangeHandler,
Identifiable {
    private final Logger log;
    private final String exchangeId;
    private final AsyncClientExchangeHandler handler;

    LoggingAsyncClientExchangeHandler(Logger log, String exchangeId, AsyncClientExchangeHandler handler) {
        this.log = log;
        this.exchangeId = exchangeId;
        this.handler = handler;
    }

    @Override
    public String getId() {
        return this.exchangeId;
    }

    @Override
    public void releaseResources() {
        this.handler.releaseResources();
    }

    @Override
    public void produceRequest(final RequestChannel channel, HttpContext context) throws HttpException, IOException {
        this.handler.produceRequest(new RequestChannel(){

            @Override
            public void sendRequest(HttpRequest request, EntityDetails entityDetails, HttpContext context) throws HttpException, IOException {
                if (LoggingAsyncClientExchangeHandler.this.log.isDebugEnabled()) {
                    LoggingAsyncClientExchangeHandler.this.log.debug("{}: send request {}, {}", LoggingAsyncClientExchangeHandler.this.exchangeId, new RequestLine(request), entityDetails != null ? "entity len " + entityDetails.getContentLength() : "null entity");
                }
                channel.sendRequest(request, entityDetails, context);
            }
        }, context);
    }

    @Override
    public int available() {
        return this.handler.available();
    }

    @Override
    public void produce(final DataStreamChannel channel) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: produce request data", (Object)this.exchangeId);
        }
        this.handler.produce(new DataStreamChannel(){

            @Override
            public void requestOutput() {
                channel.requestOutput();
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                if (LoggingAsyncClientExchangeHandler.this.log.isDebugEnabled()) {
                    LoggingAsyncClientExchangeHandler.this.log.debug("{}: produce request data, len {} bytes", (Object)LoggingAsyncClientExchangeHandler.this.exchangeId, (Object)src.remaining());
                }
                return channel.write(src);
            }

            @Override
            public void endStream() throws IOException {
                if (LoggingAsyncClientExchangeHandler.this.log.isDebugEnabled()) {
                    LoggingAsyncClientExchangeHandler.this.log.debug("{}: end of request data", (Object)LoggingAsyncClientExchangeHandler.this.exchangeId);
                }
                channel.endStream();
            }

            @Override
            public void endStream(List<? extends Header> trailers) throws IOException {
                if (LoggingAsyncClientExchangeHandler.this.log.isDebugEnabled()) {
                    LoggingAsyncClientExchangeHandler.this.log.debug("{}: end of request data", (Object)LoggingAsyncClientExchangeHandler.this.exchangeId);
                }
                channel.endStream(trailers);
            }
        });
    }

    @Override
    public void consumeInformation(HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: information response {}", (Object)this.exchangeId, (Object)new StatusLine(response));
        }
        this.handler.consumeInformation(response, context);
    }

    @Override
    public void consumeResponse(HttpResponse response, EntityDetails entityDetails, HttpContext context) throws HttpException, IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: consume response {}, {}", this.exchangeId, new StatusLine(response), entityDetails != null ? "entity len " + entityDetails.getContentLength() : " null entity");
        }
        this.handler.consumeResponse(response, entityDetails, context);
    }

    @Override
    public void updateCapacity(final CapacityChannel capacityChannel) throws IOException {
        this.handler.updateCapacity(new CapacityChannel(){

            @Override
            public void update(int increment) throws IOException {
                if (LoggingAsyncClientExchangeHandler.this.log.isDebugEnabled()) {
                    LoggingAsyncClientExchangeHandler.this.log.debug("{}: capacity update {}", (Object)LoggingAsyncClientExchangeHandler.this.exchangeId, (Object)increment);
                }
                capacityChannel.update(increment);
            }
        });
    }

    @Override
    public void consume(ByteBuffer src) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: consume response data, len {} bytes", (Object)this.exchangeId, (Object)src.remaining());
        }
        this.handler.consume(src);
    }

    @Override
    public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: end of response data", (Object)this.exchangeId);
        }
        this.handler.streamEnd(trailers);
    }

    @Override
    public void failed(Exception cause) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: execution failed: {}", (Object)this.exchangeId, (Object)cause.getMessage());
        }
        this.handler.failed(cause);
    }

    @Override
    public void cancel() {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: execution cancelled", (Object)this.exchangeId);
        }
        this.handler.cancel();
    }
}

