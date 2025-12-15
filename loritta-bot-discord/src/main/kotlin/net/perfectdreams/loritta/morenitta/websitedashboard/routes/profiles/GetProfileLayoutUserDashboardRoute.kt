package net.perfectdreams.loritta.morenitta.websitedashboard.routes.profiles

import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.profileDesignItemInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.ColorTheme

class GetProfileLayoutUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/profiles/{profileDesignId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val result = website.loritta.transaction {
            val settings = website.loritta.getLorittaProfile(session.userId)?.settings

            return@transaction Result(
                settings?.activeProfileDesignInternalName?.value ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID,
                settings?.activeBackgroundInternalName?.value ?: Background.DEFAULT_BACKGROUND_ID,
            )
        }

        val profileDesignId = call.parameters.getOrFail("profileDesignId")

        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById(LocaleManager.DEFAULT_LOCALE_ID)

        call.respondHtmlFragment {
            profileDesignItemInfo(i18nContext, locale, profileDesignId, result.activeProfileDesignId, result.activeBackgroundId)
        }
    }

    private data class Result(
        val activeProfileDesignId: String,
        val activeBackgroundId: String
    )
}