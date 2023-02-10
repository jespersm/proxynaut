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

    final static String ASTERISK = "*"
    final static Set<String> ALL_METHODS = EnumSet.allOf(HttpMethod).collect{it.name()}

    final String name
    int timeoutMs = 30_000
    String context = null
    URI uri = null
    Set<String> allowedMethods = ALL_METHODS
    Collection<String> includeRequestHeaders = Collections.emptySet()
    // TODO add cookie removal?
    Collection<String> excludeRequestHeaders = Collections.emptySet()
    Collection<String> includeResponseHeaders = Collections.emptySet()
    Collection<String> excludeResponseHeaders = Collections.emptySet()
    URL url

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
        return allowedMethods
    }

    private static String safeUpper(String s) {
        return s.toUpperCase(Locale.ENGLISH)
    }

    void setAllowedMethods(Set<String> allowedMethods) {
        if (allowedMethods.contains(ASTERISK)) {
            this.allowedMethods = ALL_METHODS
        } else {
            this.allowedMethods = allowedMethods.stream()
                    .map(ProxyConfiguration::safeUpper)
                    .filter{ALL_METHODS.contains(it)}
                    .collect(Collectors.toSet())
        }
    }

    boolean shouldAllowMethod(HttpMethod method) {
        return allowedMethods.contains(method.name())
    }

    boolean shouldIncludeRequestHeader(String headerName) {
        if (! includeRequestHeaders.empty) {
            return includeRequestHeaders.find{it.equalsIgnoreCase(headerName)} != null
        } else if (! excludeRequestHeaders.empty){
            return excludeRequestHeaders.find{it.equalsIgnoreCase(headerName)} == null
        } else {
            return true
        }
    }

    boolean shouldIncludeResponseHeader(String headerName) {
        if (! includeResponseHeaders.empty) {
            return includeResponseHeaders.find{it.equalsIgnoreCase(headerName)} != null
        } else if (! excludeResponseHeaders.empty){
            return excludeResponseHeaders.find{it.equalsIgnoreCase(headerName)} == null
        } else {
            return true
        }
    }

}
