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

import groovy.util.logging.Slf4j
import io.micronaut.context.ExecutionHandleLocator
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.web.router.DefaultRouteBuilder

import javax.inject.Singleton

@Singleton
@Slf4j
class ProxyRouteBuilder extends DefaultRouteBuilder {

    ProxyRouteBuilder(
            Collection<ProxyConfiguration> configs,
            ExecutionHandleLocator executionHandleLocator, UriNamingStrategy uriNamingStrategy) {
        super(executionHandleLocator, uriNamingStrategy)
        buildProxyRoutes(configs)
    }

    void buildProxyRoutes(Collection<ProxyConfiguration> configs) {
        if (log.isDebugEnabled()) {
            log.debug("Building proxy routes...")
        }
        for (ProxyConfiguration config : configs) {
            String contextPath = config.getContext() + "{+path:?}"
            for (HttpMethod method : HttpMethod.values()) {
                if (config.shouldAllowMethod(method)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding route: $method $contextPath")
                    }
                    buildRoute(method, contextPath, Proxy, "serve", HttpRequest, String)
                }
            }
        }
    }
}
