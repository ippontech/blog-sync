package com.ippontech.blog.common

import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClientResponseException

object RestTemplateUtils {

    private val logger = LogManager.getLogger(RestTemplateUtils::class.java)

    fun <T> handleErrors(func: () -> T): T {
        try {
            return func()
        } catch (e: RestClientResponseException) {
            logger.error(e)
            logger.error(e.responseBodyAsString)
            throw e
        }
    }

    fun createHeaders(bearerToken: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer $bearerToken")
        headers.add("User-Agent", "curl/7.54.0")
        return headers
    }
}
