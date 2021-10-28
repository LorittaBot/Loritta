package net.perfectdreams.loritta.cinnamon.common.utils

import java.net.MalformedURLException
import java.net.URL

object URLUtils {
    fun isValidURL(url: String): Boolean {
        return try {
            URL(url)
            true
        } catch (e: MalformedURLException) {
            false
        }
    }

    fun isValidSpecificProtocolURL(url: String, vararg protocols: String): Boolean {
        return try {
            val parsedUrl = URL(url)
            return parsedUrl.protocol in protocols
        } catch (e: MalformedURLException) {
            false
        }
    }

    fun isValidHttpOrHttpsURL(url: String) = isValidSpecificProtocolURL(url, "http", "https")
}