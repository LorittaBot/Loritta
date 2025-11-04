package net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.util.getOrFail
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.profilePresetsSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseAllModals
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

class DeleteProfilePresetUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/profile-presets/{profilePresetId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val profilePresetId = call.parameters.getOrFail("profilePresetId").toLong()

        val result = website.loritta.transaction {
            val deletedCount = UserCreatedProfilePresets.deleteWhere {
                UserCreatedProfilePresets.createdBy eq session.userId and (UserCreatedProfilePresets.id eq profilePresetId)
            }

            if (deletedCount == 0)
                return@transaction Result.PresetNotFound

            val profilePresets = UserCreatedProfilePresets.selectAll()
                .where {
                    UserCreatedProfilePresets.createdBy eq session.userId
                }
                .toList()

            return@transaction Result.Success(profilePresets)
        }

        when (result) {
            is Result.Success -> {
                call.respondHtmlFragment {
                    profilePresetsSection(i18nContext, result.profilePresets)

                    blissCloseAllModals()

                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Predefinição deletada!"
                        )
                    )
                }
            }
            Result.PresetNotFound -> {
                call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você não pode deletar uma predefinição que não existe!"
                        )
                    )
                }
            }
        }
    }

    private sealed class Result {
        data class Success(val profilePresets: List<ResultRow>) : Result()
        data object PresetNotFound : Result()
    }
}