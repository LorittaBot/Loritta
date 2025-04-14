package net.perfectdreams.loritta.website.backend.views

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import java.time.Instant
import java.time.format.DateTimeFormatter

abstract class BaseView(
    val LorittaWebsiteBackend: LorittaWebsiteBackend,
    val locale: BaseLocale,
    val i18nContext: I18nContext,
    val path: String
) {
    companion object {
        val versionPrefix = "/v3"
        val websiteUrl = "https://loritta.website"
    }

    val iconManager = LorittaWebsiteBackend.svgIconManager
    val hashManager = LorittaWebsiteBackend.hashManager

    fun generateHtml(): HTML.() -> (Unit) = {
        // "br" to "pt-BR", "us" to "en", "es" to "es", "pt" to "pt"
        val pageLanguage = when (locale.id) {
            "default" -> "pt-BR"
            "en-us" -> "en"
            "es" -> "es"
            else -> "en"
        }

        attributes["lang"] = pageLanguage

        head {
            meta(charset = "utf-8")
            // Necessary for responsive design on phones: If not set, the browsers uses the "Use Desktop Design"
            meta(name = "viewport", content = "width=device-width, initial-scale=1, viewport-fit=cover")

            generateMeta()

            unsafe {
                raw("""
<link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png" />
<link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png" />
<link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png" />
<link rel="manifest" href="/site.webmanifest" />
<link rel="mask-icon" href="/safari-pinned-tab.svg" color="#5bbad5" />
<meta name="msapplication-TileColor" content="#5bbad5" />
""")
            }

            styleLink("$versionPrefix/assets/css/style.css?hash=${assetHash("/assets/css/style.css")}")

            // Plausible Analytics
            script(
                src = "https://web-analytics.perfectdreams.net/js/plausible.js",
            ) {
                attributes["data-domain"] = "loritta.website"
                defer = true
            }

            // Google AdSense
            script(src = "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js") {}

            // Google ReCAPTCHA
            // script(src = "https://www.google.com/recaptcha/api.js?render=explicit&onload=onGoogleRecaptchaLoadCallback") {}

            // App itself
            deferredScript("/v3/assets/js/loritta-website-frontend.js?hash=${assetHash("/assets/js/loritta-website-frontend.js")}")

            for ((websiteLocaleId, localeName) in listOf("br" to "pt-BR", "us" to "en", "es" to "es", "pt" to "pt")) {
                link {
                    attributes["rel"] = "alternate"
                    attributes["hreflang"] = localeName
                    attributes["href"] = "$websiteUrl/$websiteLocaleId$path"
                }
            }
        }
        generateBody()
    }

    open fun getTitle(): String = "¯\\_(ツ)_/¯"
    open fun getFullTitle(): String = "${getTitle()} • Loritta"

    open fun HEAD.generateMeta() {
        // Used for Search Engines and in the browser itself
        title(getFullTitle())

        // Used for Search Engines
        meta("description", content = locale["website.genericDescription"])

        // Used for Discord Embeds
        meta("theme-color", "#29a6fe")

        // Used in Twitter
        meta(name = "twitter:card", content = "summary")
        meta(name = "twitter:site", content = "@LorittaBot")
        meta(name = "twitter:creator", content = "@MrPowerGamerBR")

        // Used for Discord Embeds
        meta(content = locale["website.lorittaWebsite"]) { attributes["property"] = "og:site_name" }
        meta(content = locale["website.genericDescription"]) { attributes["property"] = "og:description" }
        meta(content = getTitle()) { attributes["property"] = "og:title" }
        meta(content = "600") { attributes["property"] = "og:ttl" }

        val imageUrl = getImageUrl()
        if (imageUrl != null) {
            meta(content = "${LorittaWebsiteBackend.rootConfig.loritta.website}$imageUrl") { attributes["property"] = "og:image" }
            meta(name = "twitter:card", content = "summary_large_image")
        } else {
            meta(content = "https://loritta.website/assets/img/loritta_gabizinha_v1.png") { attributes["property"] = "og:image" }
        }


        val pubDate = getPublicationDate()
        if (pubDate != null) {
            meta(name = "pubdate", content = DateTimeFormatter.ISO_INSTANT.format(pubDate).toString())
        }
    }

    abstract fun HTML.generateBody()

    fun assetHash(asset: String) = hashManager.getAssetHash(asset)

    fun FlowOrInteractiveContent.customHtmlTag(htmlTag: String, block: HtmlBlockTag.() -> Unit = {}) {
        val obj = object: HTMLTag(htmlTag, consumer, emptyMap(),
            inlineTag = false,
            emptyTag = false), HtmlBlockTag {}
        obj.visit(block)
    }

    fun FlowOrInteractiveContent.sidebarWrapper(block: HtmlBlockTag.() -> Unit = {}) = customHtmlTag("lori-sidebar-wrapper", block)

    fun SectioningOrFlowContent.leftSidebar(block: NAV.() -> Unit = {}) = nav(classes = "left-sidebar") {
        id = "left-sidebar"
        attributes["data-preload-keep-scroll"] = "true"
        block.invoke(this)
    }

    fun SectioningOrFlowContent.rightSidebar(block: HtmlBlockTag.() -> Unit = {}) = section(classes = "right-sidebar") {
        block.invoke(this)
    }

    fun HEAD.deferredScript(src: String) {
        // Defer = Downloads the file during HTML parsing and will only execute after the parser has completed
        // Defer also follows the script declaration order!
        // https://stackoverflow.com/questions/10808109/script-tag-async-defer
        script(src = src) { defer = true  }
    }

    open fun getImageUrl(): String? = null
    open fun getPublicationDate(): Instant? = null
}