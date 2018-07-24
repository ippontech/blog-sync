package com.ippontech.blog.auth

data class TokenModel(
        val access_token: String,
        val refresh_token: String,
        val expires_in: Int,
        val token_type: String)
