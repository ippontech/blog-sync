package com.ippontech.blog.export

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ippontech.blog.common.githubImageBaseUrl
import org.apache.logging.log4j.LogManager
import java.io.File

fun main(args: Array<String>) {
    val outputDir = args[0]

    val ghostExportService = GhostExportService()
    val posts = ghostExportService.getAllPosts()

    val ghostToGit = GhostToGit()
    ghostToGit.process(posts, outputDir)
}

class GhostToGit {

    private val logger = LogManager.getLogger(GhostExportService::class.java)

    private val mapper = ObjectMapper().registerModule(KotlinModule())

    private val imageRegex = Regex("!\\[[^\\]]*\\]\\((/content/images/[^)]+)\\)")
    private val titleRegex = Regex("^(#+)([a-zA-Z])", RegexOption.MULTILINE)
    private val trailingSpacesRegex = Regex("\\h+$", RegexOption.MULTILINE)
    private val extraLinesRegex = Regex("(\\n\\n)\\n+", RegexOption.MULTILINE)

    fun process(posts: List<Post>, outputDir: String) {
        // read the posts and write them to separate files
        posts.forEach { writePost(outputDir, it) }
    }

    private fun writePost(outputDir: String, post: Post) {
        val outputFile = File("$outputDir/posts/${post.slug}.md")
        logger.info("Writing file: ${outputFile.canonicalPath}")

        val markdown = extractMarkdown(post)

        // write the file
        val content = generateContent(post, markdown)
        outputFile.writeText(content)

        // write the
        if (post.feature_image != null) {
            writeImage(outputDir, post.feature_image)
        }
        imageRegex.findAll(markdown)
                .forEach { writeImage(outputDir, it.groups[1]!!.value) }
    }

    private fun generateContent(post: Post, markdown: String): String {
        val authors = post.authors
                .map { "\n- ${it.name}" }
                .joinToString(separator = "")

        val tags = post.tags!!
                .map { "\n- ${it.name}" }
                .joinToString(separator = "")

        val date = if (post.status == "published") post.published_at else ""

        val title = post.title.replace("\"", "\\\"")

        val image = if (post.feature_image != null) rewriteImageUrl(post.feature_image) else ""

        val cleanMarkdown = cleanMarkdown(markdown)

        return """---
authors:$authors
tags:$tags
date: $date
title: "$title"
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
//        logger.info("  Downloading image: $url")
//
//        val headers = HttpHeaders()
//        headers.set("Accept", "*/*")
//        headers.set("User-Agent", "curl/7.54.0")
//        val entity = HttpEntity<ByteArray>(headers)
//        val response = restTemplate.exchange(url, HttpMethod.GET, entity, ByteArray::class.java)
//        //val bytes = URL("http://blog.ippon.tech$image").readBytes()
//
//        logger.info("  Writing image: ${outputFile.canonicalPath}")
//        outputPath.mkdirs()
//        outputFile.writeBytes(response.body)
    }

    // replace in markdown
    private fun rewriteImageUrls(content: String) =
            content.replace("(/content/images/", "($githubImageBaseUrl")

    // replace a single URL
    private fun rewriteImageUrl(url: String) =
            if (url.startsWith("/content/images/")) url.replace("/content/images/", githubImageBaseUrl)
            else url
}