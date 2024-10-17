// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.apache.catalina.connector.Connector
import org.apache.coyote.http11.Http11NioProtocol
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Configuration

@Configuration
class WebServerConfiguration(private val webServerProperties: WebServerProperties) :
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    override fun customize(factory: TomcatServletWebServerFactory) {
        val connector = Connector(Http11NioProtocol::class.java.name)
        connector.scheme = "http"
        connector.port = webServerProperties.port
        factory.addAdditionalTomcatConnectors(connector)
    }
}
