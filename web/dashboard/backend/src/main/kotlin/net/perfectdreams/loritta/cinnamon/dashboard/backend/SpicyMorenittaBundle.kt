package net.perfectdreams.loritta.cinnamon.dashboard.backend

import org.apache.commons.codec.digest.DigestUtils
import java.io.File

interface SpicyMorenittaBundle {
    companion object {
        fun createSpicyMorenittaJsBundleContent(spicyMorenittaJsContent: String): String {
            return spicyMorenittaJsContent
        }
    }

    fun content(): String
    fun hash(): String
}

class SpicyMorenittaProductionBundle(private val content: String) : SpicyMorenittaBundle {
    private val cachedMd5Hash: String = DigestUtils.md5Hex(content)

    override fun content() = content

    override fun hash() = cachedMd5Hash
}

class SpicyMorenittaDevelopmentBundle(private val spicyMorenittaJsPath: String) : SpicyMorenittaBundle {
    override fun content(): String {
        val spicyMorenittaJsContent = File(spicyMorenittaJsPath).readText()

        return SpicyMorenittaBundle.createSpicyMorenittaJsBundleContent(spicyMorenittaJsContent)
    }

    override fun hash() = System.currentTimeMillis().toString()
}