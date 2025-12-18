package net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.util.getOrFail
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseAllModals
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissSoundEffect
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class PostApplyProfilePresetUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/profile-presets/{profilePresetId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val profilePresetId = call.parameters.getOrFail("profilePresetId").toLong()

        val result = website.loritta.transaction {
            val presetData = UserCreatedProfilePresets.selectAll()
                .where {
                    UserCreatedProfilePresets.id eq profilePresetId and (UserCreatedProfilePresets.createdBy eq session.userId)
                }
                .firstOrNull()

            if (presetData == null)
                return@transaction Result.PresetNotFound

            val profile = website.loritta.getOrCreateLorittaProfile(session.userId)
            UserCreatedProfilePresets.update({ UserCreatedProfilePresets.id eq presetData[UserCreatedProfilePresets.id] }) {
                it[UserCreatedProfilePresets.lastUsedAt] = Instant.now()
            }
            profile.settings.activeProfileDesignInternalName = presetData[UserCreatedProfilePresets.profileDesign]
            profile.settings.activeBackgroundInternalName = presetData[UserCreatedProfilePresets.background]

            return@transaction Result.Success
        }

        when (result) {
            is Result.Success -> {
                call.respondHtmlFragment(status = HttpStatusCode.OK) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Predefinição aplicada!"
                        )
                    )
                    blissSoundEffect("configSaved")
                    blissCloseAllModals()
                }
            }
            Result.PresetNotFound -> {
                call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você não pode aplicar uma predefinição que não existe!"
                        )
                    )
                }
            }
        }
    }

    private sealed class Result {
        data object Success : Result()
        data object PresetNotFound : Result()
    }
}