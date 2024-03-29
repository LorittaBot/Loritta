package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes

import kotlinx.html.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend

// https://github.com/thedaviddias/Front-End-Checklist
fun spicyMorenittaSpaHtml(
    m: LorittaDashboardBackend,
    // i18nContext: I18nContext,
    title: String,
    // pathWithoutLocaleId: String,
    metaBlock: HEAD.() -> (Unit)
): HTML.() -> (Unit) = {
    // attributes["lang"] = i18nContext.get(I18nKeysData.WebsiteLocaleIdPath)

    head {
        meta(charset = "utf-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1, viewport-fit=cover")
        // We are sure that we will acess the DreamStorageService URL, so let's preconnect!
        // link(href = m.dreamStorageServiceClient.baseUrl, rel = "preconnect")

        title(title)

        // https://www.reddit.com/r/discordapp/comments/82p8i6/a_basic_tutorial_on_how_to_get_the_most_out_of/
        meta(name = "theme-color", "#29a6fe")
        metaBlock.invoke(this)

        // NitroPay
        script(type = ScriptType.textJavaScript) {
            unsafe {
                raw("""
window["nitroAds"] = window["nitroAds"] || {
    createAd: function() {
        window.nitroAds.queue.push(["createAd", arguments]);
    },
    queue: []
};""".trimIndent())
            }
        }
        script(src = "https://s.nitropay.com/ads-595.js") {
            defer = true // Only execute after the page has been parsed
        }

        styleLink("/assets/css/style.css?hash=${m.hashManager.getAssetHash("/assets/css/style.css")}")
        script(src = "//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js") {
            defer = true // Only execute after the page has been parsed
        }
        script(src = "/assets/js/spicy-frontend.js?hash=${m.spicyMorenittaBundle.hash()}") {
            defer = true // Only execute after the page has been parsed
        }

        link(href = "/favicon.svg", rel = "icon", type = "image/svg+xml")

        // https://stackoverflow.com/a/67476915/7271796
        /* for ((_, language) in m.languageManager.languageContexts.filter { it.value != i18nContext }) {
            // The href must be absolute!
            link(rel = "alternate", href = m.websiteUrl + "/" + language.get(I18nKeysData.WebsiteLocaleIdPath) + pathWithoutLocaleId) {
                attributes["hreflang"] = language.get(I18nKeysData.WebsiteLocaleIdPath)
            }
        } */

        script(
            src = "https://web-analytics.perfectdreams.net/js/plausible.js",
        ) {
            attributes["data-domain"] = "dash.loritta.website"
            defer = true
        }
    }

    body {
        unsafe {
            raw("""<div id="spa-loading-wrapper">
    <!-- By Sam Herbert (@sherb), for everyone. More @ http://goo.gl/7AJzbL -->
    <svg class="loading-spinner" width="38" height="38" viewBox="0 0 38 38" xmlns="http://www.w3.org/2000/svg">
        <defs>
            <linearGradient x1="8.042%" y1="0%" x2="65.682%" y2="23.865%" id="a">
                <stop stop-color="currentColor" stop-opacity="0" offset="0%"/>
                <stop stop-color="currentColor" stop-opacity=".631" offset="63.146%"/>
                <stop stop-color="currentColor" offset="100%"/>
            </linearGradient>
        </defs>
        <g fill="none" fill-rule="evenodd">
            <g transform="translate(1 1)">
                <path d="M36 18c0-9.94-8.06-18-18-18" id="Oval-2" stroke="url(#a)" stroke-width="2">
                    <animateTransform attributeName="transform" type="rotate" from="0 18 18" to="360 18 18" dur="0.9s" repeatCount="indefinite"/>
                </path>
                <circle fill="currentColor" cx="36" cy="18" r="1">
                    <animateTransform attributeName="transform" type="rotate" from="0 18 18" to="360 18 18" dur="0.9s" repeatCount="indefinite"/>
                </circle>
            </g>
        </g>
    </svg>
</div>
<div id="root"></div>""")
        }
    }
}