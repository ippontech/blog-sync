package com.ippontech.blog.import

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GithubEvent(
        val ref: String,
        val commits: Array<Commit>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Commit(
        val id: String,
        val added: Array<String>,
        val modified: Array<String>,
        val removed: Array<String>
)