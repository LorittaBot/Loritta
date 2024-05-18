package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild

import kotlinx.html.DIV
import kotlinx.html.unsafe
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.ProfileDashboardView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

/**
 * Renders a Navbar view with [html] as its' contents
 *
 * This is used for legacy Pebble views!
 */
class LegacyPebbleProfileDashboardRawHtmlView(
    lorittaWebsite: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    private val _title: String,
    private val html: String,
    private val selectedType: String,
) : ProfileDashboardView(
    lorittaWebsite,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    selectedType
) {
    override fun getTitle() = _title

    override fun DIV.generateRightSidebarContents() {
        unsafe {
            raw(html)
        }
    }
}