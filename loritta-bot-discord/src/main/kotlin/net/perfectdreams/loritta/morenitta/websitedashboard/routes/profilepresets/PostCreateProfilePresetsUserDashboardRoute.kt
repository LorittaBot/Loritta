package net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.profilepresets.PostCreateProfilePresetRoute.Companion.MAX_PRESET_LENGTH
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.profilepresets.ProfilePresetsListView
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.profilePresetsSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class PostCreateProfilePresetsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/profile-presets/create") {
    @Serializable
    data class CreateProfilePresetRequest(
        val activeProfileDesignId: String,
        val activeBackgroundId: String,
        val presetName: String
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val request = Json.decodeFromString<CreateProfilePresetRequest>(call.receiveText())
        if (request.presetName.isBlank()) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Você precisa dar um nome para a sua predefinição!"
                    )
                )
            }
            return
        }

        if (request.presetName.length !in 1..MAX_PRESET_LENGTH) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "O nome da predefinição é muito longo!"
                    )
                )
            }
            return
        }

        val result = website.loritta.transaction {
            val totalPresets = UserCreatedProfilePresets.selectAll()
                .where {
                    UserCreatedProfilePresets.createdBy eq session.userId
                }
                .count()

            if (totalPresets + 1 > ProfilePresetsListView.MAX_PROFILE_PRESETS)
                return@transaction Result.TooManyPresets

            UserCreatedProfilePresets.insert {
                it[UserCreatedProfilePresets.createdBy] = session.userId
                it[UserCreatedProfilePresets.createdAt] = Instant.now()
                it[UserCreatedProfilePresets.name] = request.presetName
                it[UserCreatedProfilePresets.profileDesign] = request.activeProfileDesignId
                it[UserCreatedProfilePresets.background] = request.activeBackgroundId
            }

            val profilePresets = UserCreatedProfilePresets.selectAll()
                .where {
                    UserCreatedProfilePresets.createdBy eq session.userId
                }
                .toList()

            return@transaction Result.Success(profilePresets)
        }

        when (result) {
            is Result.Success -> {
                call.respondHtmlFragment(status = HttpStatusCode.Created) {
                    profilePresetsSection(i18nContext, result.profilePresets)

                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Predefinição criada!"
                        )
                    )
                }
            }
            Result.TooManyPresets -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você já tem muitas predefinições criadas!"
                        )
                    )
                }
            }
        }
    }

    private sealed class Result {
        data class Success(val profilePresets: List<ResultRow>) : Result()
        data object TooManyPresets : Result()
    }
}