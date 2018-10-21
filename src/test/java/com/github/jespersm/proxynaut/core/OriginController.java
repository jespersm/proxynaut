/*
 * Copyright 2018 Jesper Steen Møller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package com.github.jespersm.proxynaut.core;

import java.util.concurrent.TimeUnit;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Controller("/origin")
public class OriginController {
	
	private final static String SPACES_100 = "                                                                                                    ";
	private final static String SPACES_1000 = SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100;  

    @Get(uri="/", produces=MediaType.TEXT_PLAIN)
    public HttpResponse<String> index() {
        return HttpResponse.ok("Origin shows all");
    }

    @Get(uri="/ok", produces=MediaType.TEXT_PLAIN)
    public HttpResponse<String> ok() {
        return HttpResponse.ok("Origin says 'ok'");
    }

    @Get(uri="/bad", produces=MediaType.TEXT_PLAIN)
    public HttpResponse<String> bad() {
        return HttpResponse.badRequest("Can't touch this");
    }

    @Get(uri="/bigResponse", produces=MediaType.TEXT_PLAIN)
    public HttpResponse<Flowable<String>> bigResponseB() {
        return HttpResponse.ok(Flowable.range(0, 10).map(i -> i.toString() + SPACES_1000)).header("Custom-Header", "42");
    }

    @Get(uri="/rawJsonStream", produces=MediaType.APPLICATION_JSON)
    public Flowable<byte[]> rawJsonStream() {
        return Flowable.just("{\"attribute\":42}".getBytes()).delay(300, TimeUnit.MILLISECONDS);
    }
}
