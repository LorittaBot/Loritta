package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild

import kotlinx.html.DIV
import kotlinx.html.unsafe
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

/**
 * Renders a Navbar view with [html] as its' contents
 *
 * This is used for legacy Pebble views!
 */
class LegacyPebbleGuildDashboardRawHtmlView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    guild: Guild,
    private val _title: String,
    private val html: String,
    selectedType: String,
) : LegacyGuildDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    guild,
    selectedType
) {
    override fun getTitle() = _title

    override fun DIV.generateRightSidebarContents() {
        unsafe {
            raw(html)
        }
    }
}