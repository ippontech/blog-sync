package com.ippontech.blog.import

import org.apache.logging.log4j.LogManager
import java.io.File

fun main(args: Array<String>) {
    val repositoryDir = args[0]
    val localGitRepoToGhost = LocalGitRepoToGhost(repositoryDir)

    localGitRepoToGhost.uploadAllPosts()

//    listOf("jhipster-5-is-out.md")
//            .forEach { localGitRepoToGhost.uploadPost(File("$repositoryDir/posts/$it")) }
}

class LocalGitRepoToGhost(val postsDir: String) {

    private val logger = LogManager.getLogger(javaClass)
    private val gitToGhost = GitToGhost()

    fun uploadAllPosts() {
        File("$postsDir/posts/")
                .listFiles()
                .filter { it.name.contains("banking") } //FIXME
                .filter { it.name.endsWith(".md") }
                .forEach { uploadPost(it) }
    }

    fun uploadPost(file: File) {
        logger.info("Processing file: ${file.name}")

        val slug = file.name.substringBefore(".md")
        val lines = File(file.path).readLines()

        gitToGhost.uploadPost(slug, lines)
    }
}