package com.github.jespersm.proxynaut.example;

import com.github.jespersm.proxynaut.core.Proxy;
import com.github.jespersm.proxynaut.core.ProxyFactory;
import com.github.jespersm.proxynaut.core.ProxyProcessor;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import javax.inject.Inject;

@ProxyFactory("hello")
public class Test implements Proxy {

    @Inject
    protected ProxyProcessor proxy;

    @Secured(SecurityRule.IS_ANONYMOUS)
    public HttpResponse<?> test(HttpRequest<ByteBuffer<?>> request, String path) {
        return proxy.serve(request, path);
    }

    public HttpResponse<?> proxy(HttpRequest<ByteBuffer<?>> request, String path) {
        return proxy.serve(request, path);
    }
}
