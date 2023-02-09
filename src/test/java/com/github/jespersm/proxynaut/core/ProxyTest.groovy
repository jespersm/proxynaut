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
import io.micronaut.core.util.CollectionUtils
import io.micronaut.runtime.server.EmbeddedServer
import org.junit.AfterClass
import org.junit.BeforeClass

class ProxyTest extends AbstractOperationTest {

    static EmbeddedServer server
    static EmbeddedServer proxyServer

    @BeforeClass
    static void makeContext()
    {
        server = ApplicationContext.run(EmbeddedServer)
        proxyServer = ApplicationContext.run(EmbeddedServer,
                PropertySource.of(
                        "testProxyConfiguration",
                        CollectionUtils.mapOf(
                            "proxynaut.test1.context", "/proxyOrigin",
                            "proxynaut.test1.uri", server.getURL()+ "/origin",
                            "proxynaut.test2.context", "/proxyJunk",
                            "proxynaut.test2.uri", server.getURL()+ "/junk")
                        )
                )
        proxyServer.start()
    }

    @AfterClass
    static void closeContext()
    {
        if (server != null) {
            server.stop()
            server = null
        }
        if (proxyServer != null) {
            proxyServer.stop()
            proxyServer = null
        }
    }

	@Override
	protected String getPrefixUnderTest() {
		return "/proxyOrigin"
	}

	@Override
	protected EmbeddedServer getServerUnderTest() {
		return proxyServer
	}

}
