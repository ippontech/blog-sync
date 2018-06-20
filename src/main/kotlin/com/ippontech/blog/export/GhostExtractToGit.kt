package com.ippontech.blog.export

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

fun main(args: Array<String>) {
    val postsFile = File(args[0])
    val usersFile = File(args[1])
    val outputDir = args[2]
    GhostExtractToGit().process(postsFile, usersFile, outputDir)
}

class GhostExtractToGit {

    val mapper = ObjectMapper().registerModule(KotlinModule())

    val imageRegex = Regex("!\\[[^\\]]*\\]\\((/content/images/[^)]+)\\)")
    val imageBaseUrl = "https://raw.githubusercontent.com/ippontech/blog-usa/master/images/"
    val titleRegex = Regex("^(#+)([a-zA-Z])", RegexOption.MULTILINE)
    val trailingSpacesRegex = Regex("\\h+$", RegexOption.MULTILINE)
    val extraLinesRegex = Regex("(\\n\\n)\\n+", RegexOption.MULTILINE)

    fun process(postsFile: File, usersFile: File, outputDir: String) {
        // read the mapping of user Ids -> user names
        val userMapping = mapper.readValue(usersFile, Users::class.java)
                .users
                .map { it.id to it.name }
                .toMap()

        // read the posts and write them to separate files
        mapper.readValue(postsFile, Posts::class.java)
                .posts
                .filter { it.mobiledoc != null }
//                .filter { it.title.contains("AppSync") }
                .forEach { writePost(outputDir, it, userMapping) }
    }

    private fun writePost(outputDir: String, post: Post, userMapping: Map<String, String>) {
        val outputFile = File("$outputDir/posts/${post.slug}.md")
        println("Writing file: ${outputFile.canonicalPath}")

        val markdown = extractMarkdown(post)

        // write the file
        val content = generateContent(post, markdown, userMapping)
        outputFile.writeText(content)

        // write the
        if (post.feature_image != null) {
            writeImage(outputDir, post.feature_image)
        }
        imageRegex.findAll(markdown)
                .forEach { writeImage(outputDir, it.groups[1]!!.value) }
    }

    private fun generateContent(post: Post, markdown: String, userMapping: Map<String, String>): String {
        val author = userMapping[post.author]

        val categories = post.tags!!.map { it.name }
                .joinToString(prefix = "- ", separator = "\n- ")

        val date = if (post.status == "published") post.published_at else ""

        val title = post.title.replace("\"", "\\\"")

        val image = if (post.feature_image != null) rewriteImageUrl(post.feature_image) else ""

        val cleanMarkdown = cleanMarkdown(markdown)

        return """---
authors:
- $author
categories:
$categories
date: $date
title: "$title"
id: ${post.id}
image: $image
---

$cleanMarkdown
"""
    }

    private fun extractMarkdown(post: Post): String {
        val mobiledoc = mapper.readValue(post.mobiledoc!!, MobileDoc::class.java)
        val markdown = mobiledoc.cards[0][1].get("markdown").textValue()!!.trim()
        return markdown
    }

    private fun cleanMarkdown(markdown: String): String {
        // rewrite URLs
        val md1 = rewriteImageUrls(markdown)

        // cleanup titles with missing spaces
        val md2 = cleanTitles(md1)

        // remove trailing spaces
        val md3 = md2.replace(trailingSpacesRegex, "")

        // remove extra blank lines
        val md4 = md3.replace(extraLinesRegex, "$1")

        return md4
    }

    private fun cleanTitles(markdown: String): String {
        var inCode = false
        val lines = markdown.split('\n')
        val resLines = mutableListOf<String>()
        for (line in lines) {
            if (line.startsWith("```")) {
                inCode = !inCode
            }
            val resLine = if (!inCode) {
                line.replace(titleRegex, "$1 $2")
            } else {
                line
            }
            resLines.add(resLine)
        }
        return resLines.joinToString(separator = "\n")
    }

    private fun writeImage(outputDir: String, image: String) {
//        if (!image.startsWith("/content/images/")) return
//
//        val path = image.substringAfter("/content/images/").substringBeforeLast("/")
//        val name = image.substringAfterLast("/")
//        val outputPath = File("$outputDir/images/$path")
//        val outputFile = File("$outputPath/$name")
//
//        val url = "http://blog.ippon.tech$image"
//        println("  Downloading image: $url")
//
//        val headers = HttpHeaders()
//        headers.set("Accept", "*/*")
//        headers.set("User-Agent", "curl/7.54.0")
//        val entity = HttpEntity<ByteArray>(headers)
//        val response = restTemplate.exchange(url, HttpMethod.GET, entity, ByteArray::class.java)
//        //val bytes = URL("http://blog.ippon.tech$image").readBytes()
//
//        println("  Writing image: ${outputFile.canonicalPath}")
//        outputPath.mkdirs()
//        outputFile.writeBytes(response.body)
    }

    // replace in markdown
    private fun rewriteImageUrls(content: String) =
            content.replace("(/content/images/", "($imageBaseUrl")

    // replace a single URL
    private fun rewriteImageUrl(url: String) =
            if (url.startsWith("/content/images/")) url.replace("/content/images/", imageBaseUrl)
            else url
}