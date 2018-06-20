package com.ippontech.blog.export

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode

@JsonIgnoreProperties(ignoreUnknown = true)
data class Posts(val posts: List<Post>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Post(
        val id: String,
        val title: String,
        val status: String,
        val slug: String,
        val author: String,
        val mobiledoc: String?,
        val created_at: String,
        val updated_at: String,
        val published_at: String?,
        val tags: List<Tag>?,
        val feature_image: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MobileDoc(val cards: JsonNode)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Tags(val tags: List<Tag>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Tag(
        val id: String,
        val name: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Users(val users: List<User>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
        val id: String,
        val name: String)
