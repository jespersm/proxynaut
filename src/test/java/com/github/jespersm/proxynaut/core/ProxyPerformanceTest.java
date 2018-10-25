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
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ReferenceCounted;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriTemplate;
import io.micronaut.runtime.server.EmbeddedServer;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;

public class ProxyPerformanceTest {

    static EmbeddedServer server;
    static EmbeddedServer proxyServer;
    RxStreamingHttpClient client;

    private static long CHUNK_COUNT = 2_000;
    private static long CHUNK_SIZE = 1_000_000;
    
    @Test
    public void testStreamBigResponse() throws InterruptedException {
    	String uri = UriTemplate.of("/proxyOrigin/randomData{?chunks,size}").expand(CollectionUtils.mapOf("chunks", CHUNK_COUNT, "size", CHUNK_SIZE));
    	
        Flowable<ByteBuffer<?>> response = client.dataStream(HttpRequest.create(HttpMethod.GET, uri));
        Maybe<Long> sumOfBufferBytes = response.map(bb -> {
        	long value = bb.readableBytes();
        	if (bb instanceof ReferenceCounted) ((ReferenceCounted)bb).release();
        	
        	return value;
        }).reduce(Long::sum);
        
        long allBytes = sumOfBufferBytes.blockingGet();
        assertEquals(CHUNK_COUNT * CHUNK_SIZE, allBytes);
    }

    @BeforeClass
    public static void makeContext()
    {
        server = ApplicationContext.run(EmbeddedServer.class);
        proxyServer = ApplicationContext.run(EmbeddedServer.class, 
                PropertySource.of(
                        "whatever",
                        CollectionUtils.mapOf(
                            "proxynaut.test1.context", "/proxyOrigin",
                            "proxynaut.test1.uri", server.getURL()+ "/origin"
                        )
                    )
                );
        proxyServer.start();
    }

    @AfterClass
    public static void closeContext()
    {
        if (server != null) {
            server.stop();
            server = null;
        }
        if (proxyServer != null) {
            proxyServer.stop();
            proxyServer = null;
        }
    }

    @Before
    public void makeClient()
    {
        client = proxyServer.getApplicationContext().createBean(RxStreamingHttpClient.class, proxyServer.getURL());
    }

    @After
    public void closeClient()
    {
        if (client != null) {
            client.stop();
            client = null;
        }
    }

}
