package net.perfectdreams.loritta.website.backend.views

import kotlinx.html.*
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend

class BlissPlaygroundView(
    LorittaWebsiteBackend: LorittaWebsiteBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : NavbarView(
    LorittaWebsiteBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    override val hasDummyNavbar = true

    override fun getTitle() = locale["website.jumbotron.tagline"]

    override fun DIV.generateContent() {
        h1 {
            text("Testing Harmony Bliss")
        }

        button {
            id = "click-to-random"
            attributes["bliss-get"] = "/br/"
            attributes["bliss-swaps"] = "#jumbotron -> #swap-to-me"

            text("Click to Random!")
        }
        div {
            id = "swap-to-me"
        }
    }
}