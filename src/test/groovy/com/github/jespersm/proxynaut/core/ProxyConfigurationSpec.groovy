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

import com.github.jespersm.proxynaut.core.ProxyConfiguration
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.PropertySource
import io.micronaut.http.HttpMethod
import spock.lang.Specification

class ProxyConfigurationSpec extends Specification {

    void "test proxy configuration"() {
        when:
        ApplicationContext applicationContext = ApplicationContext.run(PropertySource.of(
                [
                "proxynaut.test1.context": "/root",
                "proxynaut.test1.uri": "http://some.server/root",
                "proxynaut.test1.allowed-methods": ["get", "post"],
                "proxynaut.test1.include-request-headers": ["Cookie-Control"],
                "proxynaut.test1.exclude-response-headers": ["Content-Disposition"],
                "proxynaut.test2.context": "/root",
                "proxynaut.test2.uri": "http://some.server/root",
                "proxynaut.test2.allowed-methods": ["get", "put"],
                "proxynaut.test2.include-request-headers": ["Authentication"],
                "proxynaut.test2.includeResponseHeaders": ["Cookie-Control"],
                "proxynaut.test3.context": "/root",
                "proxynaut.test3.uri": "http://some.server/root",
                "proxynaut.test3.allowed-methods": ["*"],
                "proxynaut.test3.exclude-request-headers": ["Authentication"],
                "proxynaut.test3.exclude-response-headers": ["X-Powered-By"]
                ])
        )

        then:
        applicationContext.containsBean(ProxyConfiguration)
        Collection<ProxyConfiguration> proxies = applicationContext.getBeansOfType(ProxyConfiguration)

        ProxyConfiguration proxy = proxies.stream().filter(p -> p.getName().equals("test1")).findFirst().get()
        proxy.getContext().toString() == "/root"
        proxy.getUri().toString() == "http://some.server/root"
        proxy.getAllowedMethods().contains("GET")
        proxy.shouldAllowMethod(HttpMethod.GET)
        !proxy.shouldAllowMethod(HttpMethod.PUT)
        proxy.shouldIncludeRequestHeader("Cookie-Control")
        !proxy.shouldIncludeRequestHeader("Authentication")
        !proxy.shouldIncludeResponseHeader("Content-Disposition")
        proxy.shouldIncludeResponseHeader("X-Powered-By")
    }

}
