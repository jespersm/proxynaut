package com.github.jespersm.proxynaut.core;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.HttpMethod;

@EachProperty("proxynaut")  
public class ProxyConfiguration {

    private final static String ASTERISK = "*";
    private final static EnumSet<HttpMethod> ALL_METHODS = EnumSet.allOf(HttpMethod.class);
    
    private final String name;
    private int timeoutMs = 30_000;
    private String context = null;
    private URI uri = null;
    private EnumSet<HttpMethod> allowedMethods = ALL_METHODS;
    private Collection<String> includeRequestHeaders = Collections.emptySet();
    private Collection<String> excludeRequestHeaders = Collections.emptySet();
    private Collection<String> includeResponseHeaders = Collections.emptySet();
    private Collection<String> excludeResponseHeaders = Collections.emptySet();
    private URL url;

    public ProxyConfiguration(@Parameter String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) throws MalformedURLException {
        this.uri = uri;
        this.url = uri.toURL();
    }

    public Collection<String> getAllowedMethods() {
        return allowedMethods.stream().map(HttpMethod::name).collect(Collectors.toSet());
    }

    private static String safeUpper(String s) {
        return s.toUpperCase(Locale.ENGLISH);
    }
    
    public void setAllowedMethods(Collection<String> allowedMethods) {
        if (allowedMethods.contains(ASTERISK)) {
            this.allowedMethods = ALL_METHODS;
        } else {
            this.allowedMethods = EnumSet.copyOf(allowedMethods.stream()
                    .map(ProxyConfiguration::safeUpper)
                    .map(HttpMethod::valueOf)
                    .collect(Collectors.toSet()));
        }
    }

    public boolean shouldAllowMethod(HttpMethod method) {
        return allowedMethods.contains(method);
    }
    
    public Collection<String> getIncludeRequestHeaders() {
        return Collections.unmodifiableCollection(includeRequestHeaders);
    }

    public void setIncludeRequestHeaders(Collection<String> values) {
        this.includeRequestHeaders = upperCaseSet(values);
    }

    public Collection<String> getExcludeRequestHeaders() {
        return Collections.unmodifiableCollection(excludeRequestHeaders);
    }

    public void setExcludeRequestHeaders(Collection<String> values) {
        this.excludeRequestHeaders = upperCaseSet(values);
    }

    public boolean shouldIncludeRequestHeader(String headerName) {
        if (! includeRequestHeaders.isEmpty()) {
            return includeRequestHeaders.contains(safeUpper(headerName));
        } else if (! excludeRequestHeaders.isEmpty()){
            return ! excludeRequestHeaders.contains(safeUpper(headerName));
        } else {
            return true;
        }
    }

    public Collection<String> getIncludeResponseHeaders() {
        return Collections.unmodifiableCollection(includeResponseHeaders);
    }

    public void setIncludeResponseHeaders(Collection<String> values) {
        this.includeResponseHeaders = upperCaseSet(values);
    }

    public Collection<String> getExcludeResponseHeaders() {
        return Collections.unmodifiableCollection(excludeResponseHeaders);
    }

    public void setExcludeResponseHeaders(Collection<String> values) {
        this.excludeResponseHeaders = upperCaseSet(values);
    }

    public boolean shouldIncludeResponseHeader(String headerName) {
        if (! includeResponseHeaders.isEmpty()) {
            return includeResponseHeaders.contains(safeUpper(headerName));
        } else if (! excludeResponseHeaders.isEmpty()){
            return ! excludeResponseHeaders.contains(safeUpper(headerName));
        } else {
            return true;
        }
    }
    
    private Set<String> upperCaseSet(Collection<String> strings) {
        return strings.stream().map(s -> s.toUpperCase(Locale.ENGLISH)).collect(Collectors.toSet());
    }

    public URL getUrl() {
        return url;
    }

	public int getTimeoutMs() {
		return timeoutMs;
	}

	public void setTimeoutMs(int timeoutMs) {
		this.timeoutMs = timeoutMs;
	}
}
