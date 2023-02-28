package com.github.jespersm.proxynaut.example;

import com.github.jespersm.proxynaut.core.Proxy;
import io.micronaut.context.annotation.Executable;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Test {

    @Inject
    Proxy proxy;

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Executable
    public HttpResponse<?> test(HttpRequest<ByteBuffer<?>> request, String path) throws InterruptedException {
        return proxy.serve(request, path);
    }
}
