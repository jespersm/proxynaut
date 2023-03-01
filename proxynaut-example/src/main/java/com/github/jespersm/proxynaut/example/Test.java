package com.github.jespersm.proxynaut.example;

import com.github.jespersm.proxynaut.core.Proxy;
import com.github.jespersm.proxynaut.core.ProxyFactory;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import javax.inject.Inject;

@ProxyFactory
public class Test {

    @Inject
    protected Proxy proxy;

    @Secured(SecurityRule.IS_ANONYMOUS)
    public HttpResponse<?> test(HttpRequest<ByteBuffer<?>> request, String path) {
        return proxy.serve(request, path);
    }
}
