package net.perfectdreams.loritta.website.backend.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend

class DashboardView(
    LorittaWebsiteBackend: LorittaWebsiteBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : BaseView(
    LorittaWebsiteBackend,
    locale,
    i18nContext,
    path
) {
    override fun HTML.generateBody() {
        body {
            div {
                text("Test hewwo owo")
            }
        }
    }
}