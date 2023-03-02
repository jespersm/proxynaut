package com.github.jespersm.proxynaut.example;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/")
@Secured(SecurityRule.IS_ANONYMOUS)
public class AppController {

    @Get(uri="/", produces=MediaType.TEXT_PLAIN)
    public HttpResponse<String> index() {
        return HttpResponse.ok("This is the root");
    }

    @Get(uri="/page1", produces=MediaType.TEXT_PLAIN)
    public HttpResponse<String> page1() {
        return HttpResponse.ok("This is page 1 of your app");
    }
}
