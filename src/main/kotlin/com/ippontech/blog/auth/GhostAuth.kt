package com.ippontech.blog.auth

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

object GhostAuth {

    private val url = "https://test-ippon.ghost.io/ghost/api/v0.1/authentication/token"

    fun getBearerToken(): String {
        val username: String = env("GHOST_USERNAME")
        val password: String = env("GHOST_PASSWORD")
        val clientId: String = env("GHOST_CLIENT_ID")
        val clientSecret: String = env("GHOST_CLIENT_SECRET")
        return getBearerToken(username, password, clientId, clientSecret)
    }

    fun getBearerToken(username: String, password: String, clientId: String, clientSecret: String): String {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
        headers.add("User-Agent", "curl/7.54.0")

        val map = LinkedMultiValueMap<String, String>()
        map.add("grant_type", "password")
        map.add("username", username)
        map.add("password", password)
        map.add("client_id", clientId)
        map.add("client_secret", clientSecret)

        val request = HttpEntity<MultiValueMap<String, String>>(map, headers)

        val restTemplate = RestTemplate()
        val response = restTemplate.postForObject(url, request, TokenModel::class.java)

        return response.access_token
    }

    private fun env(name: String): String =
            System.getenv(name) ?: throw Exception("Environment variable '$name' is missing")
}

// only for tests
fun main(args: Array<String>) {
    val token = GhostAuth.getBearerToken()
    println(token)
}
