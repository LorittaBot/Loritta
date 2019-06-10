package net.perfectdreams.loritta.website.views

import kotlinx.html.*
import kotlinx.html.dom.create
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.website.LorittaWebsite
import org.w3c.dom.Document
import org.w3c.dom.Element

abstract class BaseView(val document: Document) {
    open fun getTitle(): String = "Loritta é a minha amiga!"

    open fun generateHead(): Element {
        return document.create.head {
            title(getTitle())
            styleLink("${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/css/style.css")
            styleLink("https://use.fontawesome.com/releases/v5.8.1/css/all.css")
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/kotlin.js") {}
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/kotlinx-coroutines-core.js") {}
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/kotlinx-serialization-runtime-js.js") {}
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/kotlinx-html-js.js") {}
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/kotlinx-io.js") {}
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/kotlinx-coroutines-io.js") {}
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/ktor-utils.js") {}
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/ktor-http.js") {}
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/ktor-client-core.js") {}
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/ktor-client-js.js") {}
            script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/js/output.js") {}
        }
    }

    abstract fun generateBody(locale: BaseLocale): Element

    fun generate(locale: BaseLocale): Element {
        val html = document.create.html { }

        html.appendChild(generateHead())
        html.appendChild(generateBody(locale))
        return html
    }
}