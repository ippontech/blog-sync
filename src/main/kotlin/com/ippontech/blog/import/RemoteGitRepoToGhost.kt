package com.ippontech.blog.import

import org.apache.logging.log4j.LogManager
import org.springframework.web.client.RestTemplate
import java.net.URI

// for tests only
fun main(args: Array<String>) {
    val path = "posts/open-banking-open-opportunities.md"
    val localGitRepoToGhost = RemoteGitRepoToGhost()
    localGitRepoToGhost.uploadPost(path)
}

class RemoteGitRepoToGhost {

    private val baseUrl = "https://raw.githubusercontent.com/ippontech/blog-usa/master"
    private val logger = LogManager.getLogger(javaClass)
    private val restTemplate = RestTemplate()
    private val gitToGhost = GitToGhost()

    fun uploadPost(path: String) {
        logger.info("Processing post: ${path}")

        val slug = path.substringAfter("/").substringBefore(".md")

        val content = restTemplate.getForObject(URI("$baseUrl/$path"), String::class.java)
        val lines = content.split("\n")

        gitToGhost.uploadPost(slug, lines)
    }
}