package com.ippontech.blog.import

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ippontech.blog.common.RestTemplateUtils.createHeaders
import com.ippontech.blog.common.RestTemplateUtils.handleErrors
import com.ippontech.blog.common.apiUrl
import com.ippontech.blog.common.githubImageBaseUrl
import com.ippontech.blog.export.GhostExportService
import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import java.io.File

fun main(args: Array<String>) {
    val repositoryDir = args[0]
    val bearerToken = args[1]
    val gitToGhost = GitToGhost(bearerToken, repositoryDir)

    gitToGhost.uploadAllPosts()

//    listOf("plop.md")
//            .forEach { gitToGhost.uploadPost(File("$repositoryDir/posts/$it")) }
}

class GitToGhost(val bearerToken: String, val postsDir: String) {

    private val logger = LogManager.getLogger(GitToGhost::class.java)
    private val restTemplate = RestTemplate()
    private val mapper = ObjectMapper().registerModule(KotlinModule())
    private val ghostExportService = GhostExportService(bearerToken)
    private val authorsNameToIdMap = ghostExportService.getAuthorsNameToIdMap()
    private val tagsNameToIdMap = ghostExportService.getTagsNameToIdMap()

    fun uploadAllPosts() {
        File("$postsDir/posts/")
                .listFiles()
                .filter { it.name.endsWith(".md") }
                .forEach { uploadPost(it) }
    }

    fun uploadPost(file: File) {
        logger.info("Processing file: ${file.name}")
        val post = createModel(file)

        logger.info("Uploading: ${post.slug}")
        val headers = createHeaders(bearerToken)
        headers.add("Content-Type", "application/json; charset=UTF-8")
        val posts = GhostPosts(listOf(post))
        val entity = HttpEntity(posts, headers)
        val (url, method) = if (post.id == null) {
            Pair("$apiUrl/posts/?include=authors,tags,authors.roles", HttpMethod.POST)
        } else {
            Pair("$apiUrl/posts/${post.id}/?include=authors,tags,authors.roles", HttpMethod.PUT)
        }
        handleErrors {
            val res = restTemplate.exchange<Object>(url, method, entity, Object::class.java)
            if (res.statusCode != HttpStatus.OK && res.statusCode != HttpStatus.CREATED) {
                logger.error("Failed uploading post '${post.slug}'")
                logger.error("Body: ${res.body}")
                throw Exception("Failed uploading post")
            }
        }
    }

    // EXAMPLE HEADER:
    //   ---
    //   authors:
    //   - Alexis Seigneurin
    //   categories:
    //   - Big Data
    //   date: 2016-09-12T08:54:54.000Z
    //   title: "Spark - Calling Scala code from PySpark"
    //   id: 5a267e57dd54250018d6b616
    //   image: https://raw.githubusercontent.com/ippontech/blog-usa/master/images/2017/01/spark-logo-1.png
    //   ---
    private fun createModel(file: File): Post {
        val slug = file.name.substringBefore(".md")
        val lines = File(file.path).readLines()

        var inHeader = false
        var inAuthors = false
        var inTags = false
        var date: String? = null
        var title: String? = null
        var image: String? = null
        val authorNames = mutableListOf<String>()
        val tagNames = mutableListOf<String>()
        val content = mutableListOf<String>()

        val it = lines.iterator()
        while (it.hasNext()) {
            val line = it.next()
            if (line == "---") {
                if (!inHeader && title == null) {
                    inHeader = true
                    continue
                }
                if (inHeader) {
                    inHeader = false
                    continue
                }
            }
            if (inHeader) {
                if (!line.startsWith("- ")) {
                    inAuthors = false
                    inTags = false
                }
                when {
                    line == "authors:" -> inAuthors = true
                    line == "tags:" -> inTags = true
                    line.startsWith("date:") ->
                        date = line.substringAfter("date:")
                                .trim()
                    line.startsWith("title:") ->
                        title = line.substringAfter("title:")
                                .trim('"', ' ')
                                .replace("\\\"", "\"")
                    line.startsWith("image:") ->
                        image = line.substringAfter("image:")
                                .trim()
                    line.startsWith("- ") -> {
                        val value = line.substringAfter("- ")
                        if (inAuthors) {
                            authorNames.add(value)
                        } else if (inTags) {
                            tagNames.add(value)
                        } else {
                            throw Exception("Item outside of authors or categories")
                        }
                    }
                    line.startsWith("id:") -> if (true) {
                    } //FIXME
                    else -> throw Exception("Header line not recognized: '$line'")
                }
                if (line == "authors:") inAuthors = true
                if (line == "tags:") inTags = true
            } else {
                content.add(line)
            }
        }

//        if (id == null) throw Exception("Post id not found in header")
        if (title == null) throw Exception("Post title not found in header")
//        if (date == null) throw Exception("Post date not found in header")
//        if (image == null) throw Exception("Post image not found in header")
        if (authorNames.isEmpty()) throw Exception("No author found in header")

        val mobiledoc = createMobileDoc(content)

        val existingPost = ghostExportService.findPost(slug)

        val featureImage = if (image != null) {
            assert(image.startsWith(githubImageBaseUrl))
            image.replace(githubImageBaseUrl, "/content/images/")
        } else {
            null
        }

        val authors = authorNames.map {
            val authorId = authorsNameToIdMap[it]
            if (authorId == null) throw Exception("Author not found in Ghost: '$it'")
            Author(authorId)
        }

        val tags = tagNames.map {
            val tagId = tagsNameToIdMap[it]
            if (tagId == null) throw Exception("Tag not found in Ghost: '$it'")
            Tag(tagId)
        }

        return Post(
                id = existingPost?.id,
                title = title,
                slug = slug,
                mobiledoc = mobiledoc,
                status = if (existingPost == null) "draft" else existingPost.status,
                created_at = if (date != null && date.isNotEmpty()) date else null,
                updated_at = existingPost?.updated_at,
                published_at = existingPost?.published_at,
                authors = authors,
                feature_image = featureImage,
                tags = tags)
    }

    private fun createMobileDoc(content: MutableList<String>): String {
        // EXAMPLE:
        // "{\"version\":\"0.3.1\",\"markups\":[],\"atoms\":[],\"cards\":[[\"card-markdown\",{\"cardName\":\"card-markdown\",\"markdown\":\"plo2\"}]],\"sections\":[[10,0]]}"

        val markdown = content.joinToString(separator = "\n").trim()
        val mobileDoc = MobileDoc(
                version = "0.3.1",
                markups = emptyList(),
                atoms = emptyList(),
                cards = listOf(listOf(
                        "card-markdown",
                        Card("card-markdown", markdown)
                )),
                sections = listOf(listOf(10, 0))
        )
        return mapper.writeValueAsString(mobileDoc)
    }

}
