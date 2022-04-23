/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package dev.ssdd.rtdb.playground.http.impl;

import java.io.IOException;
import java.net.Socket;

import dev.ssdd.rtdb.playground.http.HttpConnectionFactory;
import dev.ssdd.rtdb.playground.http.HttpRequest;
import dev.ssdd.rtdb.playground.http.HttpResponse;
import dev.ssdd.rtdb.playground.http.annotation.Contract;
import dev.ssdd.rtdb.playground.http.annotation.ThreadingBehavior;
import dev.ssdd.rtdb.playground.http.config.ConnectionConfig;
import dev.ssdd.rtdb.playground.http.entity.ContentLengthStrategy;
import dev.ssdd.rtdb.playground.http.io.HttpMessageParserFactory;
import dev.ssdd.rtdb.playground.http.io.HttpMessageWriterFactory;

/**
 * Default factory for {@link dev.ssdd.rtdb.playground.http.HttpServerConnection}s.
 *
 * @since 4.3
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultBHttpServerConnectionFactory
        implements HttpConnectionFactory<DefaultBHttpServerConnection> {

    public static final DefaultBHttpServerConnectionFactory INSTANCE = new DefaultBHttpServerConnectionFactory();

    private final ConnectionConfig cconfig;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final HttpMessageParserFactory<HttpRequest> requestParserFactory;
    private final HttpMessageWriterFactory<HttpResponse> responseWriterFactory;

    public DefaultBHttpServerConnectionFactory(
            final ConnectionConfig cconfig,
            final ContentLengthStrategy incomingContentStrategy,
            final ContentLengthStrategy outgoingContentStrategy,
            final HttpMessageParserFactory<HttpRequest> requestParserFactory,
            final HttpMessageWriterFactory<HttpResponse> responseWriterFactory) {
        super();
        this.cconfig = cconfig != null ? cconfig : ConnectionConfig.DEFAULT;
        this.incomingContentStrategy = incomingContentStrategy;
        this.outgoingContentStrategy = outgoingContentStrategy;
        this.requestParserFactory = requestParserFactory;
        this.responseWriterFactory = responseWriterFactory;
    }

    public DefaultBHttpServerConnectionFactory(
            final ConnectionConfig cconfig,
            final HttpMessageParserFactory<HttpRequest> requestParserFactory,
            final HttpMessageWriterFactory<HttpResponse> responseWriterFactory) {
        this(cconfig, null, null, requestParserFactory, responseWriterFactory);
    }

    public DefaultBHttpServerConnectionFactory(final ConnectionConfig cconfig) {
        this(cconfig, null, null, null, null);
    }

    public DefaultBHttpServerConnectionFactory() {
        this(null, null, null, null, null);
    }

    @Override
    public DefaultBHttpServerConnection createConnection(final Socket socket) throws IOException {
        final DefaultBHttpServerConnection conn = new DefaultBHttpServerConnection(
                this.cconfig.getBufferSize(),
                this.cconfig.getFragmentSizeHint(),
                ConnSupport.createDecoder(this.cconfig),
                ConnSupport.createEncoder(this.cconfig),
                this.cconfig.getMessageConstraints(),
                this.incomingContentStrategy,
                this.outgoingContentStrategy,
                this.requestParserFactory,
                this.responseWriterFactory);
        conn.bind(socket);
        return conn;
    }

}