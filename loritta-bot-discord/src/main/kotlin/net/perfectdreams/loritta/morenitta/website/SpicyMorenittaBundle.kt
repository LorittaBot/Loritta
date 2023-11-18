package net.perfectdreams.loritta.morenitta.website

import net.perfectdreams.loritta.common.utils.extensions.getPathFromResources
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import kotlin.io.path.readText

interface SpicyMorenittaBundle {
    companion object {
        fun createSpicyMorenittaJsBundleContent(spicyMorenittaJsContent: String): String {
            // Merge the contents
            val jQueryContent = LorittaWebsite::class.getPathFromResources("/web_dependencies/jquery-3.2.1.min.js")!!.readText()
            val countUpContent = LorittaWebsite::class.getPathFromResources("/web_dependencies/countUp.min.js")!!.readText()
            val showdownContent = LorittaWebsite::class.getPathFromResources("/web_dependencies/showdown.min.js")!!.readText()
            val tingleContent = LorittaWebsite::class.getPathFromResources("/web_dependencies/tingle.min.js")!!.readText()
            val autosizeContent = LorittaWebsite::class.getPathFromResources("/web_dependencies/autosize.min.js")!!.readText()
            val toastrContent = LorittaWebsite::class.getPathFromResources("/web_dependencies/toastr.min.js")!!.readText()
            val select2Content = LorittaWebsite::class.getPathFromResources("/web_dependencies/select2.min.js")!!.readText()
            val momentWithLocalesContent = LorittaWebsite::class.getPathFromResources("/web_dependencies/moment-with-locales.min.js")!!.readText()

            return "$jQueryContent\n$countUpContent\n$showdownContent\n$tingleContent\n$autosizeContent\n$toastrContent\n$select2Content\n$momentWithLocalesContent\n$spicyMorenittaJsContent"
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

class SpicyMorenittaDevelopmentBundle(private val loritta: LorittaBot) : SpicyMorenittaBundle {
    override fun content(): String {
        val spicyMorenittaJsContent = File(loritta.config.loritta.website.spicyMorenittaJsPath).readText()

        return SpicyMorenittaBundle.createSpicyMorenittaJsBundleContent(spicyMorenittaJsContent)
    }

    override fun hash() = System.currentTimeMillis().toString()
}