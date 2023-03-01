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
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ExecutionHandleLocator
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.web.router.DefaultRouteBuilder

import javax.inject.Singleton

@Singleton
@Slf4j
class ProxyRouteBuilder extends DefaultRouteBuilder {

    ApplicationContext applicationContext

    ProxyRouteBuilder(
            ProxyConfiguration config,
            ExecutionHandleLocator executionHandleLocator, UriNamingStrategy uriNamingStrategy,
            ApplicationContext applicationContext) {
        super(executionHandleLocator, uriNamingStrategy)
        this.applicationContext = applicationContext
        buildProxyRoutes(config)
    }

    void buildProxyRoutes(ProxyConfiguration config) {
        if (log.isDebugEnabled()) {
            log.debug("Building proxy routes...")
        }
        for (ProxyConfigItem item : config.proxies) {
            String contextPath = item.context + "{+path:?}"
            for (HttpMethod method : HttpMethod.values()) {
                if (item.shouldAllowMethod(method)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding route: $method $contextPath")
                    }

                    Proxy bean = applicationContext.getBean(Proxy, Qualifiers.byName(item.qualifier))
//                    buildRoute(method, contextPath, Class.forName(config.className), config.classMethod, HttpRequest, String)
                    buildRoute(method, contextPath, bean.class, item.invokeUsingMethod ?: "proxy", HttpRequest, String)
                }
            }
        }
    }
}
