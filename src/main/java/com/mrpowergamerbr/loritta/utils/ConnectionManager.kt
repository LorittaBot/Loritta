package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.threads.NewRssFeedTask.Companion.coroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val mutex = Mutex()

    var lastProxyListUpdate = 0L

    suspend fun updateProxies() {
        mutex.withLock {
            this.lastProxyListUpdate = System.currentTimeMillis()
            val proxyList = HttpRequest.get(loritta.config.connectionManager.proxySources.random())
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
            // Adicionar todos os proxies da config também
            this.proxies.addAll(
                    loritta.config.connectionManager.proxies.map {
                        val split = it.split(":")
                        Proxy(split[0], split[1].toInt())
                    }
            )

            this.proxies.addAll(validProxies)
        }
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

            return loritta.config.connectionManager.trustedDomains.any { it.endsWith(domain) }
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

            return loritta.config.connectionManager.blockedDomains.any { it.endsWith(domain) }
        } else {
            return false
        }
    }

    data class Proxy(val ip: String, val port: Int)
}

fun HttpRequest.doSafeConnection(): HttpRequest {
    val url = ConnectionManager.URL_FIELD.get(this) as URL

    if (loritta.config.connectionManager.proxyUntrustedConnections) {
        if (loritta.connectionManager.isBlocked(url.toString())) {
            if (System.currentTimeMillis() - loritta.connectionManager.lastProxyListUpdate > 900_000 && !loritta.connectionManager.mutex.isLocked) {
                GlobalScope.launch(coroutineDispatcher) {
                    loritta.connectionManager.updateProxies()
                }
            }

            // Isto não irá ajudar muito, mas ajudará a "adiar" script kiddies
            ConnectionManager.logger.info { "IP Logger detected $url, redirecing to somewhere else!" }
            return HttpRequest.get("https://loritta.website")
        }

        if (!loritta.connectionManager.isTrusted(url.toString())) {
            if (System.currentTimeMillis() - loritta.connectionManager.lastProxyListUpdate > 900_000 && !loritta.connectionManager.mutex.isLocked) {
                GlobalScope.launch(coroutineDispatcher) {
                    loritta.connectionManager.updateProxies()
                }
            }

            // This ain't trusted dawg!
            ConnectionManager.logger.info { "Doing untrusted connection $url, that ain't trusted dawg! Let's proxy it!!" }

            val proxy = loritta.connectionManager.getRandomProxy()

            this.useProxy(proxy.ip, proxy.port)
        }
    }

    return this
}

fun URL.openSafeConnection(): URLConnection {
    if (loritta.config.connectionManager.proxyUntrustedConnections) {
        if (loritta.connectionManager.isBlocked(this.toString())) {
            if (System.currentTimeMillis() - loritta.connectionManager.lastProxyListUpdate > 900_000 && !loritta.connectionManager.mutex.isLocked) {
                GlobalScope.launch(coroutineDispatcher) {
                    loritta.connectionManager.updateProxies()
                }
            }

            // Isto não irá ajudar muito, mas ajudará a "adiar" script kiddies
            ConnectionManager.logger.info { "IP Logger detected ${this}, redirecing to somewhere else!" }
            return URL("https://loritta.website").openConnection()
        }

        if (!loritta.connectionManager.isTrusted(this.toString())) {
            if (System.currentTimeMillis() - loritta.connectionManager.lastProxyListUpdate > 900_000 && !loritta.connectionManager.mutex.isLocked) {
                GlobalScope.launch(coroutineDispatcher) {
                    loritta.connectionManager.updateProxies()
                }
            }

            // This ain't trusted dawg!
            ConnectionManager.logger.info { "Doing untrusted connection ${this}, that ain't trusted dawg! Let's proxy it!!" }

            val proxy = loritta.connectionManager.getRandomProxy()

            return this.openConnection(Proxy(java.net.Proxy.Type.HTTP, InetSocketAddress(proxy.ip, proxy.port)))

        }
    }

    return this.openConnection()
}