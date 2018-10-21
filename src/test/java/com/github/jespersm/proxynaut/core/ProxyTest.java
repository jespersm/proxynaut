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
import io.micronaut.runtime.server.EmbeddedServer;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;

public class ProxyTest {

    static EmbeddedServer server;
    static EmbeddedServer proxyServer;
    RxStreamingHttpClient client;

    @Test
    public void testRoot200() throws InterruptedException {
        HttpResponse<String> response = client.toBlocking().exchange("/proxyOrigin", String.class);
        assertEquals("Origin shows all", response.body());
    }

    @Test
    public void testGET200() throws InterruptedException {
        HttpResponse<String> response = client.toBlocking().exchange("/proxyOrigin/ok", String.class);
        assertEquals("Origin says 'ok'", response.body());
    }

    @Test
    public void testGETJson() throws InterruptedException {
        HttpResponse<String> response = client.toBlocking().exchange("/proxyOrigin/rawJsonStream", String.class);
        assertEquals("{\"attribute\":42}", response.body());
    }

    @Test
    public void testStreamBigResponse() throws InterruptedException {
        Flowable<ByteBuffer<?>> response = client.dataStream(HttpRequest.create(HttpMethod.GET, "/proxyOrigin/bigResponse"));
        Maybe<String> bigStringF = response.map(bb -> {
        	String value = bb.toString(StandardCharsets.UTF_8);
        	//if (bb instanceof ReferenceCounted) ((ReferenceCounted)bb).release();
        	return value;
        }).reduce((a,b) -> a+b);
        
        String bigString = bigStringF.blockingGet();
        assertEquals(10*(1000+1), bigString.length()); 
        assertEquals(10, bigString.split(" +").length); 
    }

    @Test
    public void testGET400() throws InterruptedException {
    	try {
		    HttpResponse<String> response = client.toBlocking().exchange("/proxyOrigin/bad", String.class);
		} catch (HttpClientResponseException hcre) {
			assertEquals(HttpStatus.BAD_REQUEST, hcre.getStatus());
		}
    }

    @Test
    public void testGETJunk200() throws InterruptedException {
    	try {
    		HttpResponse<String> response = client.toBlocking().exchange("/proxyJunk", String.class);
    	} catch (HttpClientResponseException hcre) {
    		assertEquals(HttpStatus.NOT_FOUND, hcre.getStatus());
    	}
    }

    @BeforeClass
    public static void makeContext()
    {
        server = ApplicationContext.run(EmbeddedServer.class);
        proxyServer = ApplicationContext.run(EmbeddedServer.class, 
                PropertySource.of(
                        "testProxyConfiguration",
                        CollectionUtils.mapOf(
                            "proxy.test1.context", "/proxyOrigin",
                            "proxy.test1.uri", server.getURL()+ "/origin",
                            "proxy.test2.context", "/proxyJunk",
                            "proxy.test2.uri", server.getURL()+ "/junk")
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
