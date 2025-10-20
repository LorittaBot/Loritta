package net.perfectdreams.loritta.morenitta.websitedashboard.routes.backgrounds

import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.backgroundItemInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.ColorTheme

class GetBackgroundUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/backgrounds/{backgroundId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
        val result = website.loritta.transaction {
            val settings = website.loritta.getLorittaProfile(session.userId)?.settings

            return@transaction Result(
                settings?.activeProfileDesignInternalName?.value ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID,
                settings?.activeBackgroundInternalName?.value ?: Background.DEFAULT_BACKGROUND_ID,
            )
        }

        val backgroundId = call.parameters.getOrFail("backgroundId")

        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById(LocaleManager.DEFAULT_LOCALE_ID)

        call.respondHtml(
            createHTML()
                .body {
                    backgroundItemInfo(i18nContext, locale, backgroundId, result.activeProfileDesignId, result.activeBackgroundId)
                }
        )
    }

    private data class Result(
        val activeProfileDesignId: String,
        val activeBackgroundId: String
    )
}