package com.github.j1mrenwick.proxynaut.core


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

        ProxyConfiguration proxy = proxies.find{it.name == "test1"}
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
