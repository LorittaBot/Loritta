package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
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

class DeletePremiumTwitchTrackGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/twitch/premium-tracks/{premiumTrackId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val premiumTrackId = call.parameters.getOrFail("premiumTrackId").toLong()

        val result = website.loritta.transaction {
            val deletedCount = PremiumTrackTwitchAccounts.deleteWhere {
                PremiumTrackTwitchAccounts.guildId eq guild.idLong and (PremiumTrackTwitchAccounts.id eq premiumTrackId)
            }

            if (deletedCount == 0)
                return@transaction Result.ChannelNotFound

            val guildCommands = TrackedTwitchAccounts.selectAll()
                .where {
                    TrackedTwitchAccounts.guildId eq guild.idLong
                }
                .toList()

            return@transaction Result.Success(guildCommands)
        }

        when (result) {
            is Result.Success -> {
                call.respondHtmlFragment(status = HttpStatusCode.OK) {
                    blissCloseModal()

                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Acompanhamento premium deletado!"
                        )
                    )
                }
            }
            Result.ChannelNotFound -> {
                call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você não pode deletar um acompanhamento premium que não existe!"
                        )
                    )
                }
            }
        }
    }

    private sealed class Result {
        data class Success(val trackedYouTubeChannels: List<ResultRow>) : Result()
        data object ChannelNotFound : Result()
    }
}