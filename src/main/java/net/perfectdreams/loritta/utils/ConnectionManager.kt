package net.perfectdreams.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import java.net.URL

class ConnectionManager {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    fun isTrusted(url: String): Boolean {
        val pattern = Constants.URL_PATTERN
        val matcher = pattern.matcher(url)
        if (matcher.find()) {
            val everything = matcher.group(0)
            val afterSlash = matcher.group(1)
            val domain = everything.replace(afterSlash, "")

            return Loritta.config.connectionManagerConfig.trustedDomains.any { it.endsWith(domain) }
        } else {
            return false
        }
    }

    fun isBlocked(url: String): Boolean {
        val pattern = Constants.URL_PATTERN
        val matcher = pattern.matcher(url)
        if (matcher.find()) {
            val everything = matcher.group(0)
            val afterSlash = matcher.group(1)
            val domain = everything.replace(afterSlash, "")

            return Loritta.config.connectionManagerConfig.blockedDomains.any { it.endsWith(domain) }
        } else {
            return false
        }
    }
}

fun HttpRequest.doSafeConnection(): HttpRequest {
    val field = this::class.java.getDeclaredField("url")
    field.isAccessible = true
    val url = field.get(this) as URL

    if (Loritta.config.connectionManagerConfig.proxyUntrustedConnections) {
        if (!loritta.connectionManager.isTrusted(url.toString())) {
            // Isto não irá ajudar muito, mas ajudará a "adiar" script kiddies
            ConnectionManager.logger.info { "IP Logger detected $url, redirecing to somewhere else!" }
            return HttpRequest.get("https://loritta.website")
        }

        if (!loritta.connectionManager.isTrusted(url.toString())) {
            // This ain't trusted dawg!
            ConnectionManager.logger.info { "Doing untrusted connection $url, that ain't trusted dawg! Let's proxy it!!" }
            this.useProxy(Loritta.config.connectionManagerConfig.proxyIp, Loritta.config.connectionManagerConfig.proxyPort)
        }
    }
    
    return this
}