package com.github.jespersm.proxynaut.core;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.web.router.DefaultRouteBuilder;

@Singleton  
public class ProxyRouteBuilder extends DefaultRouteBuilder {

    public ProxyRouteBuilder(ExecutionHandleLocator executionHandleLocator, UriNamingStrategy uriNamingStrategy) {
        super(executionHandleLocator, uriNamingStrategy);
    }

    @Inject
    void buildProxyRoutes(Collection<ProxyConfiguration> configs) {
        for (ProxyConfiguration config : configs) { 
            String contextPath = config.getContext() + "{+path:?}";
            for (HttpMethod method : HttpMethod.values()) {
                if (! config.shouldAllowMethod(method)) continue;
                buildRoute(method, contextPath, Proxy.class, "serve", HttpRequest.class, String.class);
            }        
        }
    }
}
