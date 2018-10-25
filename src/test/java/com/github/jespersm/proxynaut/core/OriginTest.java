/*
 * Copyright 2018 Jesper Steen Møller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.jespersm.proxynaut.core;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ReferenceCounted;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriTemplate;
import io.micronaut.runtime.server.EmbeddedServer;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * The purpose of this test is to make sure that the OriginController works as expected, so that
 * ProxyTest only tests the operation of Proxy.
 */
public class OriginTest extends AbstractOperationTest {

    static EmbeddedServer server;
    static EmbeddedServer proxyServer;
    
    @BeforeClass
    public static void makeContext()
    {
        server = ApplicationContext.run(EmbeddedServer.class);
    }

    @AfterClass
    public static void closeContext()
    {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

	@Override
	protected String getPrefixUnderTest() {
		return "/origin";
	}

	@Override
	protected EmbeddedServer getServerUnderTest() {
		return server;
	}

}
