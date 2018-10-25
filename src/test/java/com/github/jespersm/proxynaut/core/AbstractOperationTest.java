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
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ReferenceCounted;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.DefaultHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriTemplate;
import io.micronaut.runtime.server.EmbeddedServer;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Maybe;

public abstract class AbstractOperationTest {

    protected RxStreamingHttpClient client;
    
    private static long CHUNK_COUNT = 2_000;
    private static long CHUNK_SIZE = 1_000_000;
    
    protected abstract String getPrefixUnderTest();
    protected abstract EmbeddedServer getServerUnderTest();
    
    @Test
    public void testStreamRandomData() throws InterruptedException {
    	String uri = UriTemplate.of(getPrefixUnderTest() + "/randomData{?chunks,size}").expand(CollectionUtils.mapOf("chunks", CHUNK_COUNT, "size", CHUNK_SIZE));
    	
        Flowable<ByteBuffer<?>> response = client.dataStream(HttpRequest.create(HttpMethod.GET, uri));
        Maybe<Long> sumOfBufferBytes = response.map(bb -> {
        	long value = bb.readableBytes();
        	if (bb instanceof ReferenceCounted) ((ReferenceCounted)bb).release();
        	
        	return value;
        }).reduce(Long::sum);
        
        long allBytes = sumOfBufferBytes.blockingGet();
        assertEquals(CHUNK_COUNT * CHUNK_SIZE, allBytes);
    }

    @Test
    public void testRoot200() throws InterruptedException {
        HttpResponse<String> response = client.toBlocking().exchange(getPrefixUnderTest() + "", String.class);
        assertEquals("Origin shows all", response.body());
    }

    @Test
    public void testGET200() throws InterruptedException {
        HttpResponse<String> response = client.toBlocking().exchange(getPrefixUnderTest() + "/ok", String.class);
        assertEquals("Origin says 'ok'", response.body());
    }

    @Test
    public void testGET400() throws InterruptedException {
        try {
            HttpResponse<String> response = client.toBlocking().exchange(getPrefixUnderTest() + "/bad", String.class);
            fail("Expected exception");
        } catch (HttpClientResponseException hcre) {
            assertEquals(HttpStatus.BAD_REQUEST, hcre.getStatus());
            // Only test this for origin server, for now
            if (! getPrefixUnderTest().contains("proxy")) { 
            	assertEquals("Can't touch this", hcre.getResponse().body());
            }
        }
    }

    @Test
    public void testGET400_exchangeStream() throws InterruptedException {
    	CompletableFuture<HttpStatus> status = new CompletableFuture<>(); 
    	
        Flowable<HttpResponse<ByteBuffer<?>>> response = client.exchangeStream(HttpRequest.GET(getPrefixUnderTest() + "/bad"));
        ((DefaultHttpClient)client).getConfiguration().setReadIdleTimeout(Duration.ofMinutes(5));
        response.subscribe(new FlowableSubscriber<HttpResponse<ByteBuffer<?>>>() {

			@Override
			public void onSubscribe(Subscription s) {
				s.request(1);
			}

			@Override
			public void onNext(HttpResponse<ByteBuffer<?>> r) {
				fail("Not expecting normal content from /bad");
				status.complete(r.getStatus());
			}

			@Override
			public void onError(Throwable t) {
				if (t instanceof HttpClientResponseException) {
					// body is null here
					status.complete(((HttpClientResponseException)t).getStatus());
				} else {
					status.completeExceptionally(t);
				}
			}

			@Override
			public void onComplete() {
				fail("Not expecting content from /bad to complete normally");
			}
		});
        try {
			assertEquals(HttpStatus.BAD_REQUEST, status.get());
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testGETJunk200() throws InterruptedException {
        try {
            HttpResponse<String> response = client.toBlocking().exchange("/junk", String.class);
        } catch (HttpClientResponseException hcre) {
            assertEquals(HttpStatus.NOT_FOUND, hcre.getStatus());
        }
    }

    @Test
    public void testStreamBigResponse() throws InterruptedException {
        Flowable<ByteBuffer<?>> response = client.dataStream(HttpRequest.create(HttpMethod.GET, getPrefixUnderTest() + "/bigResponse"));
        String bigString = response.map(bb -> {
        	String value = bb.toString(StandardCharsets.UTF_8);
        	if (bb instanceof ReferenceCounted) ((ReferenceCounted)bb).release();
        	return value;
        }).reduce((a,b) -> a+b).blockingGet();
        assertEquals(10*(1000+1), bigString.length()); 
        assertEquals(10, bigString.split(" +").length); 
    }

    @Test
    public void testRawJsonStream() throws InterruptedException {
    	String response = client.toBlocking().retrieve(getPrefixUnderTest() + "/rawJsonStream");
        assertEquals("{\"attribute\":42}", response); 
    }
    
    @Before
    public void makeClient()
    {
        EmbeddedServer embeddedServer = getServerUnderTest();
		client = embeddedServer.getApplicationContext().createBean(RxStreamingHttpClient.class, embeddedServer.getURL());
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
