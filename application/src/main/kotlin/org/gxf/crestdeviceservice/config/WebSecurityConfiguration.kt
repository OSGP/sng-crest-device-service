// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration {
    @Bean
    fun filterChain(http: HttpSecurity, webAppPortMatcher: RequestMatcher): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize(webAppPortMatcher, permitAll) // TODO authenticated
                authorize(anyRequest, permitAll)
            }
            formLogin {
                loginPage = "/web/login"
            }
            csrf {
                disable()
            }
        }
        return http.build()
    }

    @Bean
    fun webAppPortMatcher(webServerProperties: WebServerProperties): RequestMatcher = RequestMatcher { request ->
        request.localPort == webServerProperties.port
    }
}
