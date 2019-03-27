package com.ippontech.blog.common

import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import org.apache.logging.log4j.LogManager
import java.io.IOException

class SlackClient {
    private val logger = LogManager.getLogger(javaClass)
    private val session: SlackSession = SlackSessionFactory.createWebSocketSlackSession(System.getenv("SLACK_TOKEN"))

    init {
        try {
            session.connect()
        } catch (e: IOException) {
            logger.error("Unable to create Slack client", e)
        }
    }

    fun sendMessage(channel: String, message: String) {
        if (session.isConnected) {
            session.sendMessage(session.findChannelByName(channel), message)
        }
    }

}
