package com.ippontech.blog.import

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.logging.log4j.LogManager
import java.util.*

// Entry point to run as an AWS Lambda
class LambdaHandler : RequestHandler<Map<String, Any>, String> {

    private val logger = LogManager.getLogger(javaClass)
    private val remoteGitRepoToGhost = RemoteGitRepoToGhost()

    override fun handleRequest(input: Map<String, Any>, context: Context): String {
        logger.info("Handler called with input: $input")

        val pushRef = input["ref"]
        if (pushRef == null || pushRef != "refs/heads/master") {
            logger.warn("Commit info is not for the master branch - Stopping")
            return "NOT_MASTER"
        }

        val commits = input["commits"] as ArrayList<Any>
        commits.map { processCommits(it as Map<String, Any>) }

        logger.info("Done")
        return "SUCCESS"
    }

    private fun processCommits(commit: Map<String, Any>) {
        val commitId = commit["id"]!!
        logger.info("Commit ID: $commitId")

        val modifiedFiles = commit["modified"]!! as ArrayList<String>
        modifiedFiles.filter { it.startsWith("posts/") }
                .map { updatePost(it) }
    }

    private fun updatePost(path: String) {
        logger.info("Updating post: $path")
        remoteGitRepoToGhost.uploadPost(path)
    }
}
