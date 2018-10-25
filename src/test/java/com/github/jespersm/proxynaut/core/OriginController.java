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

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.reactivex.Flowable;

@Controller("/origin")
public class OriginController {
	
	private final static String SPACES_100 = "                                                                                                    ";
	private final static String SPACES_1000 = SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100 + SPACES_100;  

    protected static final Logger LOG = LoggerFactory.getLogger(OriginController.class);

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
    	LOG.info("Can't touch this!");
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
    
    @Get(uri="/randomData{?chunks,size}", produces=MediaType.APPLICATION_OCTET_STREAM)
    public Flowable<byte[]> randomData(@QueryValue @Nullable Integer chunks, @QueryValue @Nullable Integer size) {
    	byte[] randomData = new byte[size];
    	new Random(System.currentTimeMillis()).nextBytes(randomData);
        return Flowable.just(randomData).repeat(chunks);
    }

}
