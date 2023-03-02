package com.github.j1mrenwick.proxynaut.core
/**
 * The purpose of this test is to make sure that the OriginController works as expected, so that
 * ProxyTest only tests the operation of Proxy.
 */
class OriginSpec extends AbstractOperation {

    @Override
	protected String getPrefixUnderTest() {
		return "/origin"
	}

}
