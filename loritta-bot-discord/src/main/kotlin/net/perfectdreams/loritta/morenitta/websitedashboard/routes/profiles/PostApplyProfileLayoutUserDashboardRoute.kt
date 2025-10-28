package net.perfectdreams.loritta.morenitta.websitedashboard.routes.profiles

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.profileDesignItemInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.update

class PostApplyProfileLayoutUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/profiles/{profileDesignId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val profileDesignId = call.parameters["profileDesignId"]

        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById(LocaleManager.DEFAULT_LOCALE_ID)

        val result = website.loritta.transaction {
            UserSettings.innerJoin(Profiles).update({ Profiles.id eq session.userId }) {
                it[UserSettings.activeProfileDesign] = profileDesignId
            }

            val settings = website.loritta.getLorittaProfile(session.userId)?.settings

            return@transaction Result(
                settings?.activeProfileDesignInternalName?.value ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID,
                settings?.activeBackgroundInternalName?.value ?: Background.DEFAULT_BACKGROUND_ID
            )
        }

        call.respondHtmlFragment {
            blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Layout de Perfil aplicado!"))

            profileDesignItemInfo(i18nContext, locale, result.activeProfileDesignId, result.activeProfileDesignId, result.activeBackgroundId)
        }
    }

    data class Result(
        val activeProfileDesignId: String,
        val activeBackgroundId: String
    )
}