/*
 * Copyright 2018 Jesper Steen Møller
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package com.github.jespersm.proxynaut.core

import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Parameter
import io.micronaut.http.HttpMethod

import java.util.stream.Collectors

@EachProperty("proxynaut")
class ProxyConfiguration {

    private final static String ASTERISK = "*"
    private final static Set<HttpMethod> ALL_METHODS = EnumSet.allOf(HttpMethod)

    private final String name
    private int timeoutMs = 30_000
    private String context = null
    private URI uri = null
    private Set<HttpMethod> allowedMethods = ALL_METHODS
    private Collection<String> includeRequestHeaders = Collections.emptySet()
    // TODO add cookie removal?
    private Collection<String> excludeRequestHeaders = Collections.emptySet()
    private Collection<String> includeResponseHeaders = Collections.emptySet()
    private Collection<String> excludeResponseHeaders = Collections.emptySet()
    private URL url

    ProxyConfiguration(@Parameter String name) {
        this.name = name
    }

    String getName() {
        return name
    }

    String getContext() {
        return context
    }

    void setContext(String context) {
        this.context = context
    }

    URI getUri() {
        return uri
    }

    void setUri(URI uri) throws MalformedURLException {
        this.uri = uri
        this.url = uri.toURL()
    }

    Collection<String> getAllowedMethods() {
        return allowedMethods.stream().map(HttpMethod::name).collect(Collectors.toSet())
    }

    private static String safeUpper(String s) {
        return s.toUpperCase(Locale.ENGLISH)
    }

    void setAllowedMethods(Collection<String> allowedMethods) {
        if (allowedMethods.contains(ASTERISK)) {
            this.allowedMethods = ALL_METHODS
        } else {
            this.allowedMethods = EnumSet.copyOf(allowedMethods.stream()
                    .map(ProxyConfiguration::safeUpper)
                    .map(HttpMethod::valueOf)
                    .collect(Collectors.toSet()))
        }
    }

    boolean shouldAllowMethod(HttpMethod method) {
        return allowedMethods.contains(method)
    }

//    Collection<String> getIncludeRequestHeaders() {
//        return Collections.unmodifiableCollection(includeRequestHeaders)
//    }
//
//    void setIncludeRequestHeaders(Collection<String> values) {
//        this.includeRequestHeaders = upperCaseSet(values)
//    }
//
//    Collection<String> getExcludeRequestHeaders() {
//        return Collections.unmodifiableCollection(excludeRequestHeaders)
//    }
//
//    void setExcludeRequestHeaders(Collection<String> values) {
//        this.excludeRequestHeaders = upperCaseSet(values)
//    }

    boolean shouldIncludeRequestHeader(String headerName) {
        if (! includeRequestHeaders.empty) {
            return includeRequestHeaders.find{it.equalsIgnoreCase(headerName)} != null
        } else if (! excludeRequestHeaders.empty){
            return excludeRequestHeaders.find{it.equalsIgnoreCase(headerName)} == null
        } else {
            return true
        }
    }

//    Collection<String> getIncludeResponseHeaders() {
//        return Collections.unmodifiableCollection(includeResponseHeaders)
//    }
//
//    void setIncludeResponseHeaders(Collection<String> values) {
//        this.includeResponseHeaders = upperCaseSet(values)
//    }
//
//    Collection<String> getExcludeResponseHeaders() {
//        return Collections.unmodifiableCollection(excludeResponseHeaders)
//    }
//
//    void setExcludeResponseHeaders(Collection<String> values) {
//        this.excludeResponseHeaders = upperCaseSet(values)
//    }

    boolean shouldIncludeResponseHeader(String headerName) {
        if (! includeResponseHeaders.empty) {
            return includeResponseHeaders.find{it.equalsIgnoreCase(headerName)} != null
        } else if (! excludeResponseHeaders.empty){
            return excludeResponseHeaders.find{it.equalsIgnoreCase(headerName)} == null
        } else {
            return true
        }
    }

    private Set<String> upperCaseSet(Collection<String> strings) {
        return strings.stream().map(s -> s.toUpperCase(Locale.ENGLISH)).collect(Collectors.toSet())
    }

    URL getUrl() {
        return url
    }

	int getTimeoutMs() {
		return timeoutMs
	}

	void setTimeoutMs(int timeoutMs) {
		this.timeoutMs = timeoutMs
	}
}
