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
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.reactivex.Flowable;

public class OriginTest {

    static EmbeddedServer server;
    static EmbeddedServer proxyServer;
    RxStreamingHttpClient client;
/*
    @Test
    public void testRoot200() throws InterruptedException {
        HttpResponse<String> response = client.toBlocking().exchange("/origin", String.class);
        assertEquals("Origin shows all", response.body());
    }

    @Test
    public void testGET200() throws InterruptedException {
        HttpResponse<String> response = client.toBlocking().exchange("/origin/ok", String.class);
        assertEquals("Origin says 'ok'", response.body());
    }

    @Test
    public void testGET400() throws InterruptedException {
        try {
            HttpResponse<String> response = client.toBlocking().exchange("/origin/bad", String.class);
        } catch (HttpClientResponseException hcre) {
            assertEquals(HttpStatus.BAD_REQUEST, hcre.getStatus());
            assertEquals("Can't touch this", hcre.getResponse().body());
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
        Flowable<ByteBuffer<?>> response = client.dataStream(HttpRequest.create(HttpMethod.GET, "/origin/bigResponse"));
        String bigString = response.map(bb -> {
        	String value = bb.toString(StandardCharsets.UTF_8);
        	if (bb instanceof ReferenceCounted) ((ReferenceCounted)bb).release();
        	return value;
        }).reduce((a,b) -> a+b).blockingGet();
        assertEquals(10*(1000+1), bigString.length()); 
        assertEquals(10, bigString.split(" +").length); 
    }
*/
    @Test
    public void testRawJsonStream() throws InterruptedException {
    	String response = client.toBlocking().retrieve("/origin/rawJsonStream");
        assertEquals("{\"attribute\":42}", response); 
    }
    
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

    @Before
    public void makeClient()
    {
        client = server.getApplicationContext().createBean(RxStreamingHttpClient.class, server.getURL());
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
