package com.github.jespersm.proxynaut.core;

import io.micronaut.context.annotation.Executable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Singleton;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Executable
@Singleton
public @interface ProxyFactory {
}
