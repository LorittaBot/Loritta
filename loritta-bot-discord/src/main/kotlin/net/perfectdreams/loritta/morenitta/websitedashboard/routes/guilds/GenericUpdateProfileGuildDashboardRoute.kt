package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.getOrFail
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme

abstract class GenericUpdateProfileGuildDashboardRoute(
    website: LorittaDashboardWebServer,
    socialNetworkProfilePathPart: String,
) : RequiresGuildAuthDashboardLocalizedRoute(website, "/$socialNetworkProfilePathPart/{entryId}") {
    @Serializable
    data class UpdateProfileTrackRequest(
        val channelId: Long,
        val message: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val request = Json.decodeFromString<UpdateProfileTrackRequest>(call.receiveText())

        val result = website.loritta.transaction {
            updateProfile(guild, entryId, request)
        }

        when (result) {
            Result.Success -> {
                call.respondHtmlFragment {
                    configSaved(i18nContext)
                }
            }
            Result.EntryNotFound -> {
                call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você não pode editar uma conta que não existe!"
                        )
                    )
                }
            }
        }
    }

    abstract fun updateProfile(
        guild: Guild,
        entryId: Long,
        request: UpdateProfileTrackRequest
    ): Result

    sealed class Result {
        data object Success : Result()
        data object EntryNotFound : Result()
    }
}