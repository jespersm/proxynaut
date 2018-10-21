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

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import com.github.jespersm.proxynaut.core.ProxyConfiguration;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpMethod;

public class ProxyConfigurationTest {

    @Test
    public void testProxyConfiguration() {
        ApplicationContext applicationContext = ApplicationContext.run(PropertySource.of(
                "test",
                CollectionUtils.mapOf(
                "proxy.test1.context", "/root",
                "proxy.test1.uri", "http://some.server/root",
                "proxy.test1.allowedMethods", asList("get", "post"),
                "proxy.test1.includeRequestHeaders", asList("Cookie-Control"),
                "proxy.test1.excludeResponseHeaders", asList("Content-Disposition"),
                "proxy.test2.context", "/root",
                "proxy.test2.uri", "http://some.server/root",
                "proxy.test2.allowedMethods", asList("get", "put"),
                "proxy.test2.includeRequestHeaders", asList("Authentication"),
                "proxy.test2.includeResponseHeaders", asList("Cookie-Control"),
                "proxy.test3.context", "/root",
                "proxy.test3.uri", "http://some.server/root",
                "proxy.test3.allowedMethods", asList("*"),
                "proxy.test3.excludeRequestHeaders", asList("Authentication"),
                "proxy.test3.excludeResponseHeaders", asList("X-Powered-By")
                )
        ));
        assertTrue(applicationContext.containsBean(ProxyConfiguration.class));
        Collection<ProxyConfiguration> proxies = applicationContext.getBeansOfType(ProxyConfiguration.class);

        // Yeah, let it throw if not there
        ProxyConfiguration proxy = proxies.stream().filter(p -> p.getName().equals("test1")).findFirst().get();
        assertEquals("/root", proxy.getContext().toString());
        assertEquals( "http://some.server/root", proxy.getUri().toString());
        assertTrue("Expect to see 'GET' as allowed verb", proxy.getAllowedMethods().contains("GET"));
        assertTrue("Expect that 'GET' is allowed", proxy.shouldAllowMethod(HttpMethod.GET));
        assertFalse("Expect that 'PUT' is not allowed", proxy.shouldAllowMethod(HttpMethod.PUT));
        assertTrue("Expected that the 'Cookie-Control'-header should be sent through",
                proxy.shouldIncludeRequestHeader("Cookie-Control"));
        assertFalse("Expected that the 'Authentication'-header should NOT be sent through",
                proxy.shouldIncludeRequestHeader("Authentication"));
        assertFalse("Expected that the 'Content-Disposition'-response header should be dropped",
                proxy.shouldIncludeResponseHeader("Content-Disposition"));
        assertTrue("Expected that the 'X-Powered-By'-response header should pass through",
                proxy.shouldIncludeResponseHeader("X-Powered-By"));
    }

}
