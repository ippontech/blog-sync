package com.ippontech.blog.import

data class WebhookResult(
        val statusCode: Int,
        val body: String,
        val isBase64Encoded: Boolean = false,
        val headers: Map<String, String> = emptyMap()
)
