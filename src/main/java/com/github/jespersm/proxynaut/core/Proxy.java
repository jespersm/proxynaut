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

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Executable;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.simple.SimpleHttpRequestFactory;
import io.micronaut.http.simple.SimpleHttpResponseFactory;
import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.MulticastProcessor;

@Singleton
public class Proxy implements Closeable {

    private final Collection<ProxyConfiguration> configs;

    private Map<String, RxStreamingHttpClient> proxyMap = Collections.synchronizedMap(new HashMap<>());

    private BeanContext beanContext;

    public Proxy(Collection<ProxyConfiguration> configs, BeanContext beanContext) throws MalformedURLException {
        this.configs = configs;
        this.beanContext = beanContext;
    }

    protected static final Logger LOG = LoggerFactory.getLogger(Proxy.class);

    @Executable
    public HttpResponse<Flowable<byte[]>> serve(HttpRequest<ByteBuffer<?>> request, @Nullable String path) throws InterruptedException {
        if (path == null) {
            path = "";
        }
        String requestPath = request.getPath();
        String proxyContextPath = requestPath.substring(0, requestPath.length() - path.length());
        Optional<ProxyConfiguration> config = findConfigForRequest(proxyContextPath);
        if (!config.isPresent()) {
            // This should never happen, only if Micronaut's router somehow was confused
            return HttpResponse.status(HttpStatus.BAD_REQUEST, "Unknown proxy path: " + proxyContextPath);
        }

        RxStreamingHttpClient client = findOrCreateClient(config.get());

        String originPath = config.get().getUri().getPath() + path;
        LOG.debug("Proxy'ing incoming " + request.getMethod() + " " + request.getPath() + " -> " + originPath);
        MutableHttpRequest<Object> upstreamRequest = SimpleHttpRequestFactory.INSTANCE.create(request.getMethod(),
                originPath);

        Optional<?> body = request.getBody();
        if (HttpMethod.permitsRequestBody(request.getMethod())) {
            body.ifPresent((Object b) -> upstreamRequest.body(b));
        }
        LOG.debug("About to pivot proxy call to " + config.get().getUri() + path);
        Flowable<HttpResponse<ByteBuffer<?>>> upstreamResponseFlowable = client.exchangeStream(upstreamRequest).serialize();
        
        CompletableFuture<HttpResponse<Flowable<byte[]>>> futureResponse = new CompletableFuture<>();
        MulticastProcessor<ByteBuffer<?>> responseBodyFlowable = MulticastProcessor.create();
        responseBodyFlowable.startUnbounded();
        
        upstreamResponseFlowable.subscribe(new Subscriber<HttpResponse<ByteBuffer<?>>>() {

			private Subscription subscription;

			@Override
			public void onSubscribe(Subscription s) {
				this.subscription = s;
				s.request(Long.MAX_VALUE);
			}

			@Override
			public void onNext(HttpResponse<ByteBuffer<?>> upstreamResponse) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("************ Read Response from {}", upstreamResponse.body().toString(StandardCharsets.UTF_8));
				}
				// When the upstream first first packet comes in, complete the response
				if (! futureResponse.isDone()) {
					HttpResponse response = makeResponse(upstreamResponse, responseBodyFlowable, config);
					futureResponse.complete(response);
				}
				responseBodyFlowable.onNext(upstreamResponse.body());
				subscription.request(10);
			}

			@Override
			public void onError(Throwable t) {
				LOG.info("Error from upstream", t);
				if (t instanceof HttpClientResponseException) {
					HttpClientResponseException upstreamException = (HttpClientResponseException) t;
			    	HttpResponse upstreamErrorResponse = upstreamException.getResponse();
			    	MutableHttpResponse<?> response = SimpleHttpResponseFactory.INSTANCE.status(
			    			upstreamErrorResponse.getStatus(), Flowable.just(upstreamException.getMessage())).contentType(MediaType.TEXT_PLAIN);
					futureResponse.complete((HttpResponse<Flowable<byte[]>>) response);
				}
				responseBodyFlowable.onError(t);
			}

			@Override
			public void onComplete() {
				LOG.trace("Upstream response body done");
				responseBodyFlowable.onComplete();
			}
		});
        
        try {
			return futureResponse.get(config.get().getTimeoutMs(), TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (TimeoutException e) {
			LOG.info("Timeout occurred before getting upstream headers (configured to {} millisecond(s)", config.get().getTimeoutMs());
			return HttpResponse.status(HttpStatus.BAD_GATEWAY);
		}
    }

	protected HttpResponse makeResponse(HttpResponse<?> upstreamResponse,
    		Flowable<ByteBuffer<?>> responseFlowable,
			Optional<ProxyConfiguration> config) {
		MutableHttpResponse<Flowable<ByteBuffer<?>>> httpResponse = SimpleHttpResponseFactory.INSTANCE.status(upstreamResponse.getStatus(), responseFlowable);
		upstreamResponse.getContentType().ifPresent(mediaType -> httpResponse.contentType(mediaType));
		return httpResponse;
	}

	private RxStreamingHttpClient findOrCreateClient(ProxyConfiguration config) {
        return proxyMap.computeIfAbsent(config.getName(), n -> {
            LOG.debug("Creating proxy for " + config.getUrl());
            return beanContext.createBean(RxStreamingHttpClient.class, config.getUrl());
        });
    }

    private Optional<ProxyConfiguration> findConfigForRequest(String prefix) {
        return configs.stream().filter(config -> config.getContext().equals(prefix)).findFirst();
    }

    @Override
    public void close() throws IOException {
        proxyMap.values().forEach(client -> client.stop());
        proxyMap.clear();
    }
}
