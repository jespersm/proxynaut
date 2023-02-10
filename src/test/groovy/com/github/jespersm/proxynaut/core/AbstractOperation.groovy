/*
 * Copyright 2018 Jesper Steen Møller
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package com.github.jespersm.proxynaut.core

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.PropertySource
import io.micronaut.core.io.buffer.ByteBuffer
import io.micronaut.core.io.buffer.ReferenceCounted
import io.micronaut.core.util.CollectionUtils
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.client.netty.DefaultHttpClient
import io.micronaut.http.uri.UriTemplate
import io.micronaut.runtime.server.EmbeddedServer
import io.reactivex.subscribers.TestSubscriber
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.time.Duration

abstract class AbstractOperation extends Specification {

    private static long CHUNK_COUNT = 2_000
    private static long CHUNK_SIZE = 1_000_000

    protected abstract String getPrefixUnderTest()

    @Shared
    RxStreamingHttpClient client

    @Shared
    EmbeddedServer proxyServer

    @Shared
    EmbeddedServer destinationServer

    def setupSpec() {
        destinationServer = ApplicationContext.run(EmbeddedServer)
        proxyServer = ApplicationContext.run(EmbeddedServer,
                PropertySource.of([
                                "proxynaut.test1.context": "/proxyOrigin",
                                "proxynaut.test1.uri": "${destinationServer.getURL()}/origin",
                                "proxynaut.test2.context": "/proxyJunk",
                                "proxynaut.test2.uri": "${destinationServer.getURL()}/junk"
                        ])
        )

        client = new DefaultHttpClient(proxyServer.getURL())
    }

    def cleanupSpec() {
        if (destinationServer != null) {
            destinationServer.stop()
            destinationServer = null
        }
        if (proxyServer != null) {
            proxyServer.stop()
            proxyServer = null
        }
        if (client != null) {
            client.stop()
            client = null
        }
    }

    void "testStreamRandomData"() {
        given:
    	String uri = UriTemplate.of(getPrefixUnderTest() + "/randomData{?chunks,size}").expand(CollectionUtils.mapOf("chunks", CHUNK_COUNT, "size", CHUNK_SIZE))

        when:
        long allBytes = client.dataStream(HttpRequest.create(HttpMethod.GET, uri))
                .map(bb -> bb.readableBytes()).reduce(Long::sum).blockingGet()

        then:
        allBytes == CHUNK_COUNT * CHUNK_SIZE
    }

    void "testRoot200"() {
        when:
        HttpResponse<String> response = client.toBlocking().exchange(getPrefixUnderTest() + "", String)

        then:
        response.body() == "Origin shows all"
    }

    void "testGET200"() {
        when:
        HttpResponse<String> response = client.toBlocking().exchange(getPrefixUnderTest() + "/ok", String)

        then:
        response.body() == "Origin says 'ok'"
    }

    void "testGET400"() {
        when:
        client.toBlocking().exchange(getPrefixUnderTest() + "/bad", String)

        then:
        Exception e = thrown()
        e instanceof HttpClientResponseException
        HttpClientResponseException hcre = e as HttpClientResponseException
        hcre.getStatus() == HttpStatus.BAD_REQUEST
        if (! getPrefixUnderTest().contains("proxy")) {
            assert hcre.getResponse().body() == "Can't touch this"
        }
    }

    void "testGET400_exchangeStream"() {
        given:
        client.getConfiguration().setReadIdleTimeout(Duration.ofMinutes(5))

        when:
        TestSubscriber<HttpResponse<ByteBuffer<?>>> response = client.exchangeStream(HttpRequest.GET(getPrefixUnderTest() + "/bad")).test()

        then:
        response.awaitTerminalEvent()
        response.assertNoValues()
        response.assertNotComplete()
        response.assertError(HttpClientResponseException)
        (response.errors().first() as HttpClientResponseException).status == HttpStatus.BAD_REQUEST
    }

    void "testGETJunk200"() {
        when:
        client.toBlocking().exchange("/junk", String)

        then:
        Exception e = thrown()
        e instanceof HttpClientResponseException
        (e as HttpClientResponseException).status == HttpStatus.NOT_FOUND
    }

    void "testStreamBigResponse"() {
        when:
        List<String> dataList = client.dataStream(HttpRequest.create(HttpMethod.GET, getPrefixUnderTest() + "/bigResponse"))
                .map(bb -> {
                    String value = bb.toString(StandardCharsets.UTF_8)
                    if (bb instanceof ReferenceCounted) ((ReferenceCounted)bb).release()
                    return value
                })
                .collectInto([], (a,b) -> a.add(b))
                .blockingGet()

        then:
        String dataString = ""
        dataList.each{dataString += it}
        dataString.length() == 10*(1000+1)
    }

    void "testRawJsonStream"() {
        when:
    	String response = client.toBlocking().retrieve(getPrefixUnderTest() + "/rawJsonStream")

        then:
        response == "{\"attribute\":42}"
    }

}
