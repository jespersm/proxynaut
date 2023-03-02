package com.github.j1mrenwick.proxynaut.core

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("proxynaut")
class ProxyConfiguration {

    List<ProxyConfigItem> proxies

}
