package net.perfectdreams.loritta.morenitta.websitedashboard.routes.backgrounds

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.backgroundItemInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissSoundEffect
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.update

class PostApplyBackgroundUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/backgrounds/{backgroundId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val backgroundId = call.parameters["backgroundId"]

        if (backgroundId == "custom" && !userPremiumPlan.customBackground) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Você precisa ter premium para fazer isto!"
                    )
                )
            }
            return
        }

        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById(LocaleManager.DEFAULT_LOCALE_ID)

        val result = website.loritta.transaction {
            UserSettings.innerJoin(Profiles).update({ Profiles.id eq session.userId }) {
                it[UserSettings.activeBackground] = backgroundId
            }

            val settings = website.loritta.getLorittaProfile(session.userId)?.settings

            return@transaction Result(
                settings?.activeProfileDesignInternalName?.value ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID,
                settings?.activeBackgroundInternalName?.value ?: Background.DEFAULT_BACKGROUND_ID
            )
        }

        call.respondHtmlFragment {
            blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Background aplicado!"))
            blissSoundEffect("configSaved")
            backgroundItemInfo(i18nContext, locale, result.activeBackgroundId, result.activeProfileDesignId, result.activeBackgroundId)
        }
    }

    data class Result(
        val activeProfileDesignId: String,
        val activeBackgroundId: String
    )
}