package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteAssetsHashes

abstract class BaseView(
    val i18nContext: I18nContext,
    val locale: BaseLocale,
    val path: String
) {
    companion object {
        val versionPrefix = "/v2"
        val websiteUrl = LorittaWebsite.INSTANCE.config.websiteUrl
        val legacyBaseLocaleKeysUsedInTheFrontend = setOf(
            "DASHBOARD_NoPermission",
            "KEYWORD_STREAMER",
            "RAFFLE_YouEarned",
            "LORITTA_ADDED_ON_SERVER",
            "DASHBOARD_RoleByIntegration",
            "DASHBOARD_NoPermission",
            "BAN_PunishName",
            "KICK_PunishName",
            "MUTE_PunishName",
            "WARN_PunishName",
            "UNBAN_PunishName",
            "UNMUTE_PunishName"
        )
    }

    open val useOldStyleCss = false
    open val useDashboardStyleCss = false

    fun generateHtml(): String {
        val supportUrl = "https://loritta.website/support"
        val firefoxUrl = "https://www.mozilla.org/firefox"
        val chromeUrl = "https://www.google.com/chrome"
        val edgeUrl = "https://www.microsoft.com/edge"

        return StringBuilder().appendHTML().html {
            head {
                // Usado para login: A SpicyMorenitta usa esse código ao autenticar via "auth_popup.kts"
                // Já que é meio difícil chamar códigos de Kotlin/JS na parent window, existe esse método auxiliar para facilitar.
                // authenticate(p) sendo p = "user identification do Discord"
                // Também tem umas coisinhas do Google reCAPTCHA
                script(type = ScriptType.textJavaScript) {
                    unsafe {
                        raw("""
function authenticate(p) { output.net.perfectdreams.spicymorenitta.utils.AuthUtils.handlePostAuth(p); };

document.domain = "loritta.website";

function onGoogleRecaptchaLoadCallback() { this['spicy-morenitta'].net.perfectdreams.spicymorenitta.utils.GoogleRecaptchaUtils.onRecaptchaLoadCallback(); };

function onCloudflareTurnstileLoadCallback() { this['spicy-morenitta'].net.perfectdreams.spicymorenitta.utils.CloudflareTurnstileUtils.onRecaptchaLoadCallback(); };

window.addEventListener('load', function () {
    // Verificar se o usuário está usando o antigo Edge ou MSIE, já que nós não suportamos nenhum desses dois
    // ; MSIE == MS Internet Explorer
    // Trident/7.0 == MSIE11
    if (/(?:\b(MS)?IE\s+|\bTrident\/7\.0;.*\s+rv:|\bEdge\/)(\d+)/.test(navigator.userAgent)) {
        alert("${locale.getList("website.unsupportedBrowser").joinToString("\\n\\n", transform = { java.text.MessageFormat.format(it, supportUrl, firefoxUrl, chromeUrl, edgeUrl); })}")
    }
    // Verificar se o SpicyMorenitta foi carregado corretamente
    if (window.spicyMorenittaLoaded === undefined) {
        alert("${locale.getList("website.failedToLoadScripts").joinToString("\\n\\n", transform = { java.text.MessageFormat.format(it, supportUrl, firefoxUrl, chromeUrl, edgeUrl); })}")
    }
});
""")
                    }
                }

                title(getFullTitle())

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
                meta(name = "viewport", content = "width=device-width, initial-scale=1")

                if (useOldStyleCss) {
                    styleLink(
                        "${LorittaWebsite.INSTANCE.config.websiteUrl}/assets/css/style.css?hash=${
                            assetHashFromResources(
                                "assets/css/style.css"
                            )
                        }"
                    )
                } else if (useDashboardStyleCss) {
                    styleLink(
                        "/lori-slippy/assets/css/style.css?hash=${
                            assetHashFromResources(
                                "/lori-slippy/assets/css/style.css"
                            )
                        }"
                    )
                } else {
                    styleLink(
                        "${LorittaWebsite.INSTANCE.config.websiteUrl}$versionPrefix/assets/css/style.css?hash=${
                            assetHashFromResources(
                                "v2/assets/css/style.css"
                            )
                        }"
                    )
                }

                styleLink("https://use.fontawesome.com/releases/v6.2.0/css/all.css")

                // Google Analytics
                script(src = "https://www.googletagmanager.com/gtag/js?id=UA-53518408-9") {}

                script(type = ScriptType.textJavaScript) {
                    unsafe {
                        raw("""window.dataLayer = window.dataLayer || []; function gtag(){dataLayer.push(arguments);} gtag('js', new Date()); gtag('config', 'UA-53518408-9');""")
                    }
                }

                // Plausible
                script(
                    src = "https://web-analytics.perfectdreams.net/js/plausible.js",
                ) {
                    attributes["data-domain"] = LorittaWebsite.INSTANCE.config.websiteUrl
                        .replace("http://", "")
                        .replace("https://", "")
                        .replace("/", "")
                    defer = true
                }

                // Detect AdBlock
                script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}$versionPrefix/adsbygoogle.js") {}

                // htmx & hyperscript & other libs are included in the app.js bundle
                // TODO - htmx-adventures: ^ Currently this is false because for some reason something is going wrong saying that htmx's "process" function does not work...
                //  see the "htmx.kt" file
                script(src = "https://unpkg.com/htmx.org@2.0.2") {}

                script {
                    unsafe {
                        raw(LorittaBot::class.java.getResourceAsStream("/tsuki.js").readAllBytes().toString(Charsets.UTF_8))
                    }
                }

                // App itself
                // TODO - htmx-adventures: Defer this and other scripts of this page
                //  (we cannot do this yet because some of the old server config pages call scripts inline, and that borks everything!)
                script(src = "${LorittaWebsite.INSTANCE.config.websiteUrl}$versionPrefix/assets/js/app.js?hash=${LorittaWebsite.INSTANCE.spicyMorenittaBundle.hash()}") {
                    // defer = true
                }

                // Google AdSense
                script(src = "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js") {}

                // Google ReCAPTCHA
                script(src = "https://www.google.com/recaptcha/api.js?render=explicit&onload=onGoogleRecaptchaLoadCallback") {}

                // Cloudflare Turnstile
                script(src = "https://challenges.cloudflare.com/turnstile/v0/api.js?onload=onCloudflareTurnstileLoadCallback") {}

                // NitroPay
                unsafe {
                    raw("""
<script type="text/javascript">
  window["nitroAds"] = window["nitroAds"] || {
    createAd: function() {
      window.nitroAds.queue.push(["createAd", arguments]);
    },
    queue: []
  };
</script>
<script async src="https://s.nitropay.com/ads-595.js"></script>
                    """.trimIndent())
                }

                for ((websiteLocaleId, localeName) in listOf("br" to "pt-BR", "us" to "en", "es" to "es", "pt" to "pt")) {
                    link {
                        attributes["rel"] = "alternate"
                        attributes["hreflang"] = localeName
                        attributes["href"] = "$websiteUrl/$websiteLocaleId/$path"
                    }
                }

                meta(
                    name = "htmx-config",
                    content = buildJsonObject {
                        putJsonArray("attributesToSettle") {
                            // default settle attributes
                            add("class")
                            add("style")
                            add("width")
                            add("height")
                            // custom settle attributes!
                            add("disabled")
                            add("checked")
                        }

                        // Our pages don't really play well with going back in history
                        put("historyCacheSize", 0)
                    }.toString()
                )

                generateMeta()
            }
            generateBody()
        }.toString()
    }

    open fun getTitle(): String = "¯\\_(ツ)_/¯"
    open fun getFullTitle(): String = "${getTitle()} • Loritta"

    open fun HEAD.generateMeta() {
        meta("theme-color", "#00c1df")
        meta(name = "twitter:card", content = "summary")
        meta(name = "twitter:site", content = "@LorittaBot")
        meta(name = "twitter:creator", content = "@MrPowerGamerBR")
        meta(content = locale["website.lorittaWebsite"]) { attributes["property"] = "og:site_name" }
        meta(content = locale["website.genericDescription"]) { attributes["property"] = "og:description" }
        meta(content = getTitle()) { attributes["property"] = "og:title" }
        meta(content = "600") { attributes["property"] = "og:ttl" }
        meta(content = "https://loritta.website/assets/img/loritta_gabizinha_v1.png") { attributes["property"] = "og:image" }
    }

    abstract fun HTML.generateBody()

    fun assetHash(asset: String) = WebsiteAssetsHashes.getAssetHash(asset)
    fun assetHashFromResources(asset: String) = WebsiteAssetsHashes.getAssetHashFromResources(asset)
}