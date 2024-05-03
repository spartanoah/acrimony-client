/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.util.Set;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncFilterChain;
import org.apache.hc.core5.http.nio.AsyncFilterHandler;
import org.apache.hc.core5.http.nio.AsyncPushProducer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.nio.entity.AsyncEntityProducers;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public final class TerminalAsyncServerFilter
implements AsyncFilterHandler {
    private final HandlerFactory<AsyncServerExchangeHandler> handlerFactory;

    public TerminalAsyncServerFilter(HandlerFactory<AsyncServerExchangeHandler> handlerFactory) {
        this.handlerFactory = Args.notNull(handlerFactory, "Handler factory");
    }

    @Override
    public AsyncDataConsumer handle(HttpRequest request, EntityDetails entityDetails, HttpContext context, final AsyncFilterChain.ResponseTrigger responseTrigger, AsyncFilterChain chain) throws HttpException, IOException {
        final AsyncServerExchangeHandler exchangeHandler = this.handlerFactory.create(request, context);
        if (exchangeHandler != null) {
            exchangeHandler.handleRequest(request, entityDetails, new ResponseChannel(){

                @Override
                public void sendInformation(HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
                    responseTrigger.sendInformation(response);
                }

                @Override
                public void sendResponse(HttpResponse response, final EntityDetails entityDetails, HttpContext httpContext) throws HttpException, IOException {
                    responseTrigger.submitResponse(response, entityDetails != null ? new AsyncEntityProducer(){

                        @Override
                        public void failed(Exception cause) {
                            exchangeHandler.failed(cause);
                        }

                        @Override
                        public boolean isRepeatable() {
                            return false;
                        }

                        @Override
                        public long getContentLength() {
                            return entityDetails.getContentLength();
                        }

                        @Override
                        public String getContentType() {
                            return entityDetails.getContentType();
                        }

                        @Override
                        public String getContentEncoding() {
                            return entityDetails.getContentEncoding();
                        }

                        @Override
                        public boolean isChunked() {
                            return entityDetails.isChunked();
                        }

                        @Override
                        public Set<String> getTrailerNames() {
                            return entityDetails.getTrailerNames();
                        }

                        @Override
                        public int available() {
                            return exchangeHandler.available();
                        }

                        @Override
                        public void produce(DataStreamChannel channel) throws IOException {
                            exchangeHandler.produce(channel);
                        }

                        @Override
                        public void releaseResources() {
                            exchangeHandler.releaseResources();
                        }
                    } : null);
                }

                @Override
                public void pushPromise(HttpRequest promise, AsyncPushProducer pushProducer, HttpContext httpContext) throws HttpException, IOException {
                    responseTrigger.pushPromise(promise, pushProducer);
                }
            }, context);
            return exchangeHandler;
        }
        responseTrigger.submitResponse(new BasicHttpResponse(404), AsyncEntityProducers.create("Not found"));
        return null;
    }
}

