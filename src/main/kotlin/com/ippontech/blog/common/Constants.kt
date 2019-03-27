package com.ippontech.blog.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

val apiUrl = "https://test-ippon.ghost.io/ghost/api/v0.1"
val githubImageBaseUrl = "https://raw.githubusercontent.com/ippontech/blog-usa/master/images/"

val mapper = ObjectMapper().registerModule(KotlinModule())
val slackClient = SlackClient()
