package net.perfectdreams.loritta.morenitta.website.views

import com.google.gson.JsonArray
import kotlinx.html.DIV
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class DonateView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    val userIdentification: LorittaJsonWebSession.UserIdentification?,
    val keys: JsonArray
) : NavbarView(
    loritta,
    i18nContext,
    locale,
    path
) {
    companion object {
        const val LOCALE_PREFIX = "website.donate"
    }

    override fun getTitle() = locale["website.donate.title"]

    override fun DIV.generateContent() {}
}