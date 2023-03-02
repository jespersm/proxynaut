package com.github.j1mrenwick.proxynaut.core


import io.micronaut.http.HttpMethod

import java.util.stream.Collectors

class ProxyConfigItem {

    final static String ASTERISK = "*"
    final static Set<String> ALL_METHODS = EnumSet.allOf(HttpMethod).collect{it.name()}

    String invokeUsingMethod
    String qualifier
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

    void setUri(URI uri) throws MalformedURLException {
        this.uri = uri
        this.url = uri.toURL()
    }

    private static String safeUpper(String s) {
        return s.toUpperCase(Locale.ENGLISH)
    }

    void setAllowedMethods(Set<String> allowedMethods) {
        if (allowedMethods.contains(ASTERISK)) {
            this.allowedMethods = ALL_METHODS
        } else {
            this.allowedMethods = allowedMethods.stream()
                    .map(ProxyConfigItem::safeUpper)
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
