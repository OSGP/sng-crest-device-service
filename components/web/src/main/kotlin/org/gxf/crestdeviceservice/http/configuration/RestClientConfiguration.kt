// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.http.configuration

import org.gxf.crestdeviceservice.http.configuration.properties.HttpProperties
import org.springframework.boot.autoconfigure.web.client.RestClientSsl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfiguration(private val httpProperties: HttpProperties) {

    @Bean
    fun restClientBuilder(
        builder: RestClient.Builder,
        restClientSsl: RestClientSsl,
    ): RestClient.Builder =
        builder
            .requestFactory(requestFactory())
            .baseUrl(httpProperties.url)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

    private fun requestFactory() =
        JdkClientHttpRequestFactory().apply { setReadTimeout(httpProperties.connectionTimeout) }
}
