package com.github.j1mrenwick.proxynaut.core

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
                    buildRoute(method, contextPath, bean.class, item.invokeUsingMethod ?: "proxy", HttpRequest, String)
                }
            }
        }
    }
}
