package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.Loritta
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.net.URLConnection

class ConnectionManager {
    companion object {
        val logger = KotlinLogging.logger {}
        val URL_FIELD by lazy {
            HttpRequest::class.java.getDeclaredField("url").apply { this.isAccessible = true }
        }
    }

    val proxies = mutableListOf<Proxy>()
    var lastProxyListUpdate = 0L

    fun updateProxies() {
        this.lastProxyListUpdate = System.currentTimeMillis()
        val proxyList = HttpRequest.get("https://proxy.rudnkh.me/txt")
                .body()

        val ipAndPortProxies = proxyList.lines()
        val proxies = ipAndPortProxies.mapNotNull {
            val split = it.split(":")

            if (split.size != 2)
                return@mapNotNull null

            Proxy(split[0], split[1].toInt())
        }

        val validProxies = checkProxies(proxies)
        this.proxies.clear()
        this.proxies.addAll(validProxies)
    }

    fun checkProxies(proxies: List<Proxy>): List<Proxy> {
        val validProxies = mutableListOf<Proxy>()

        proxies.forEach {
            try {
                val code = HttpRequest.get("http://example.com")
                        .useProxy(it.ip, it.port)
                        .connectTimeout(500)
                        .readTimeout(500)
                        .code()

                if (code == 200)
                    validProxies.add(it)
            } catch (e: Exception) {
                logger.info("Removing dead proxy $it")
                this.proxies.remove(it)
            }
        }

        return validProxies
    }

    fun getRandomProxy(): Proxy {
        if (proxies.isEmpty())
            throw RuntimeException("No proxies available!")

        return proxies.random()
    }

    fun isTrusted(url: String): Boolean {
        val pattern = Constants.URL_PATTERN
        val matcher = pattern.matcher(url)
        if (matcher.find()) {
            val everything = matcher.group(0)
            val afterSlash = matcher.group(1)

            val split = everything.replace(afterSlash, "").split(".")
            if (2 > split.size)
                return false
            val domain = split.takeLast(2).joinToString(".")

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

            val split = everything.replace(afterSlash, "").split(".")
            if (2 > split.size)
                return false
            val domain = split.takeLast(2).joinToString(".")

            return Loritta.config.connectionManagerConfig.blockedDomains.any { it.endsWith(domain) }
        } else {
            return false
        }
    }

    data class Proxy(val ip: String, val port: Int)
}

fun HttpRequest.doSafeConnection(): HttpRequest {
    val url = ConnectionManager.URL_FIELD.get(this) as URL

    if (Loritta.config.connectionManagerConfig.proxyUntrustedConnections) {
        if (System.currentTimeMillis() - loritta.connectionManager.lastProxyListUpdate > 900_000) {
            loritta.connectionManager.updateProxies()
        }

        if (loritta.connectionManager.isBlocked(url.toString())) {
            // Isto não irá ajudar muito, mas ajudará a "adiar" script kiddies
            ConnectionManager.logger.info { "IP Logger detected $url, redirecing to somewhere else!" }
            return HttpRequest.get("https://loritta.website")
        }

        if (!loritta.connectionManager.isTrusted(url.toString())) {
            // This ain't trusted dawg!
            ConnectionManager.logger.info { "Doing untrusted connection $url, that ain't trusted dawg! Let's proxy it!!" }

            val proxy = loritta.connectionManager.getRandomProxy()

            this.useProxy(proxy.ip, proxy.port)
        }
    }

    return this
}

fun URL.openSafeConnection(): URLConnection {
    if (Loritta.config.connectionManagerConfig.proxyUntrustedConnections) {
        if (System.currentTimeMillis() - loritta.connectionManager.lastProxyListUpdate > 900_000) {
            loritta.connectionManager.updateProxies()
        }

        if (loritta.connectionManager.isBlocked(this.toString())) {
            // Isto não irá ajudar muito, mas ajudará a "adiar" script kiddies
            ConnectionManager.logger.info { "IP Logger detected ${this}, redirecing to somewhere else!" }
            return URL("https://loritta.website").openConnection()
        }

        if (!loritta.connectionManager.isTrusted(this.toString())) {
            // This ain't trusted dawg!
            ConnectionManager.logger.info { "Doing untrusted connection ${this}, that ain't trusted dawg! Let's proxy it!!" }

            val proxy = loritta.connectionManager.getRandomProxy()

            return this.openConnection(Proxy(java.net.Proxy.Type.HTTP, InetSocketAddress(proxy.ip, proxy.port)))

        }
    }

    return this.openConnection()
}