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
import io.micronaut.core.io.buffer.ReferenceCounted
import io.micronaut.core.util.CollectionUtils
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.netty.DefaultHttpClient
import io.micronaut.http.uri.UriTemplate
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Shared
import spock.lang.Specification

class ProxyPerformanceSpec extends Specification {

    @Shared
    EmbeddedServer server

    @Shared
    EmbeddedServer proxyServer

    @Shared
    RxStreamingHttpClient client

    private static long CHUNK_COUNT = 2_000
    private static long CHUNK_SIZE = 1_000_000

    def setupSpec()
    {
        server = ApplicationContext.run(EmbeddedServer)
        proxyServer = ApplicationContext.run(EmbeddedServer,
                PropertySource.of(
                        [
                                "proxynaut.test1.context": "/proxyOrigin",
                                "proxynaut.test1.uri": "${server.getURL()}/origin"
                        ])
        )
        proxyServer.start()

        client = new DefaultHttpClient(proxyServer.getURL())
    }

    def cleanupSpec()
    {
        if (server != null) {
            server.stop()
            server = null
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

    void "testStreamBigResponse"() {
        given:
    	String uri = UriTemplate.of("/proxyOrigin/randomData{?chunks,size}").expand(CollectionUtils.mapOf("chunks", CHUNK_COUNT, "size", CHUNK_SIZE))

        when:
        long allBytes = client.dataStream(HttpRequest.create(HttpMethod.GET, uri))
                .map(bb -> {
                    long value = bb.readableBytes()
                    if (bb instanceof ReferenceCounted) ((ReferenceCounted)bb).release()
                    return value
                }).reduce(Long::sum).blockingGet()

        then:
        allBytes == CHUNK_COUNT * CHUNK_SIZE
    }

}
