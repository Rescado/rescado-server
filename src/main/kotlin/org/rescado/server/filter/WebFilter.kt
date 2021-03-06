package org.rescado.server.filter

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.servlet.LocaleResolver
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class WebFilter(
    private val localeResolver: LocaleResolver
) : Filter {

    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val request = req as HttpServletRequest
        val response = res as HttpServletResponse

        response.setHeader(
            HttpHeaders.CONTENT_LANGUAGE,
            localeResolver.resolveLocale(request).toString().replace("_", "-")
        )

        response.setHeader(
            "Motto",
            "adopt-dont-shop"
        )

        chain.doFilter(req, res)
    }
}
