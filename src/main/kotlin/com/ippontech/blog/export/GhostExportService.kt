package com.ippontech.blog.export

import com.ippontech.blog.common.RestTemplateUtils.createHeaders
import com.ippontech.blog.common.RestTemplateUtils.handleErrors
import com.ippontech.blog.common.apiUrl
import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

class GhostExportService(val bearerToken: String) {

    private val logger = LogManager.getLogger(GhostExportService::class.java)
    private val restTemplate = RestTemplate()

    fun getAllPosts(): List<Post> {
        val headers = createHeaders(bearerToken)
        headers.add("Content-Type", "application/json; charset=UTF-8")
        val entity = HttpEntity<String>(headers)
        val url = "$apiUrl/posts/?limit=10000&status=all&formats=mobiledoc,plaintext&include=tags,authors"
        return handleErrors {
            val res = restTemplate.exchange<Posts>(url, HttpMethod.GET, entity, Posts::class.java)
            res.body!!.posts
        }
    }

    fun getPost(postId: String): Post {
        val headers = createHeaders(bearerToken)
        headers.add("Content-Type", "application/json; charset=UTF-8")
        val entity = HttpEntity<String>(headers)
        val url = "$apiUrl/posts/$postId/?status=all"
        return handleErrors {
            val res = restTemplate.exchange<Posts>(url, HttpMethod.GET, entity, Posts::class.java)
            if (res.body!!.posts.size != 1) {
                throw Exception("Post id=$postId not found, found ${res.body.posts.size} results")
            }
            res.body!!.posts.first()
        }
    }

    fun findPost(slug: String): Post? {
        val headers = createHeaders(bearerToken)
        headers.add("Content-Type", "application/json; charset=UTF-8")
        val entity = HttpEntity<String>(headers)
        val url = "$apiUrl/posts/slug/$slug/?status=all&include=authors"
        try {
            val res = restTemplate.exchange<Posts>(url, HttpMethod.GET, entity, Posts::class.java)
            if (res.body!!.posts.size != 1) {
                throw Exception("Post slug=$slug not found, found ${res.body.posts.size} results")
            }
            return res.body!!.posts.first()
        } catch (e: HttpStatusCodeException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                return null
            }
            logger.error(e)
            logger.error(e.responseBodyAsString)
            throw e
        }
    }

    fun getAllAuthors(): List<Author> {
        logger.info("Fetching authors from Ghost")
        val headers = createHeaders(bearerToken)
        val entity = HttpEntity<String>(headers)
        val url = "$apiUrl/users/?limit=all"
        return handleErrors {
            val res = restTemplate.exchange<Authors>(url, HttpMethod.GET, entity, Authors::class.java)
            logger.info("Done fetching authors from Ghost")
            res.body!!.users
        }
    }

    fun getAuthorsNameToIdMap(): Map<String, String> {
        val authors = getAllAuthors()
        return authors.map { it.name to it.id }.toMap()
    }

    fun getTags(): List<Tag> {
        logger.info("Fetching tags from Ghost")
        val headers = createHeaders(bearerToken)
        val entity = HttpEntity<String>(headers)
        val url = "$apiUrl/tags/?limit=1000"
        return handleErrors {
            val res = restTemplate.exchange<Tags>(url, HttpMethod.GET, entity, Tags::class.java)
            logger.info("Done fetching tags from Ghost")
            res.body!!.tags
        }
    }

    fun getTagsNameToIdMap(): Map<String, String> {
        val tags = getTags()
        return tags.map { it.name to it.id }.toMap()
    }

}