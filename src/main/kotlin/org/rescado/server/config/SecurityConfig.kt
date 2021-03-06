package org.rescado.server.config

import org.rescado.server.constant.SecurityConstants.AUTH_ROUTE
import org.rescado.server.constant.SecurityConstants.PUBLIC_ROUTE
import org.rescado.server.filter.AuthenticationFilter
import org.rescado.server.service.AccountService
import org.rescado.server.service.AdminService
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.web.servlet.HandlerExceptionResolver

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
class SecurityConfig(
    private val adminService: AdminService,
    private val accountService: AccountService,
    private val handlerExceptionResolver: HandlerExceptionResolver
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.cors().and()
            .csrf()
            /**/.disable()
            .sessionManagement()
            /**/.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
            /**/.anyRequest().permitAll().and()
            .regexMatcher("^(?!\\$AUTH_ROUTE|\\$PUBLIC_ROUTE).*")
            /**/.addFilter(
                AuthenticationFilter(
                    authenticationManager = authenticationManager(),
                    adminService = adminService,
                    accountService = accountService,
                    handlerExceptionResolver = handlerExceptionResolver
                )
            )
    }
}
