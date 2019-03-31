package com.ippontech.blog.import

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.module.kotlin.readValue
import com.ippontech.blog.common.mapper
import com.ippontech.blog.common.slackClient
import org.apache.logging.log4j.LogManager
import java.net.URLDecoder

// Entry point to run as an AWS Lambda
class LambdaHandler : RequestHandler<Map<String, Any>, WebhookResult> {

    private val logger = LogManager.getLogger(javaClass)

    override fun handleRequest(input: Map<String, Any>, context: Context): WebhookResult {
        try {
            logger.info("Handler called with input: $input")

            val headers = input["headers"] as Map<String, Any>
            val eventType = headers["X-GitHub-Event"] as String
            if (eventType != "push") {
                logger.warn("Not a push event (received '$eventType') - Stopping")
                return WebhookResult(500, "Not a push event")
            }

            val body = input["body"] as String
            val payload = URLDecoder.decode(body.substringAfter("payload="), "UTF-8")
            val event = mapper.readValue<GithubEvent>(payload)

            if (event.ref != "refs/heads/master") {
                logger.warn("Commit info is not for the master branch - Stopping")
                return WebhookResult(500, "Commit info is not for the master branch")
            }

            val pushEventHandler = PushEventHandler()
            pushEventHandler.processCommit(event.head_commit)

            logger.info("Done")
            return WebhookResult(200, "Success")
        } catch (e: Exception) {
            slackClient.sendMessage("_us-blog", "Error while syncing blog, check logs at https://console.aws.amazon.com/cloudwatch/home?region=us-east-1#logStream:group=/aws/lambda/blog-sync-prod-process")
            throw e
        }
    }
}

class PushEventHandler {

    private val logger = LogManager.getLogger(javaClass)
    private val remoteGitRepoToGhost = RemoteGitRepoToGhost()

    fun processCommit(commit: Commit) {
        logger.info("Commit ID: ${commit.id}")

        processFiles(commit.added, commit.id)
        processFiles(commit.modified, commit.id)
    }

    private fun processFiles(files: Array<String>, commitId: String) {
        files.filter { it.startsWith("posts/") }
                .map { updatePost(it, commitId) }
    }

    private fun updatePost(path: String, commitId: String) {
        logger.info("Updating post: path=$path, commitId=$commitId")
        remoteGitRepoToGhost.uploadPost(path, commitId)
    }
}
