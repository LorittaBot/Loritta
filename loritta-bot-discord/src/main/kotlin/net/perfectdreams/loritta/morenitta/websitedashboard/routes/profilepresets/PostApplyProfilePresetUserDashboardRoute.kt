package net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.util.getOrFail
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class PostApplyProfilePresetUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/profile-presets/{profilePresetId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
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
                call.respondHtml(
                    createHTML()
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.SUCCESS,
                                    "Predefinição aplicada!"
                                )
                            )

                            blissCloseModal()
                        },
                    status = HttpStatusCode.OK
                )
            }
            Result.PresetNotFound -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "Você não pode aplicar uma predefinição que não existe!"
                                )
                            )
                        },
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }

    private sealed class Result {
        data object Success : Result()
        data object PresetNotFound : Result()
    }
}