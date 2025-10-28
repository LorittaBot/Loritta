package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.TrackedProfile
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedYouTubeChannelsSection
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
import kotlin.collections.map

class DeleteYouTubeChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/youtube/{channelId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val channelId = call.parameters.getOrFail("channelId").toLong()

        val result = website.loritta.transaction {
            val deletedCount = TrackedYouTubeAccounts.deleteWhere {
                TrackedYouTubeAccounts.guildId eq guild.idLong and (TrackedYouTubeAccounts.id eq channelId)
            }

            if (deletedCount == 0)
                return@transaction Result.ChannelNotFound

            val guildCommands = TrackedYouTubeAccounts.selectAll()
                .where {
                    TrackedYouTubeAccounts.guildId eq guild.idLong
                }
                .toList()

            return@transaction Result.Success(guildCommands)
        }

        when (result) {
            is Result.Success -> {
                val youtubeChannelsInfo = result.trackedYouTubeChannels.map {
                    GlobalScope.async {
                        YouTubeWebUtils.getYouTubeChannelInfoFromChannelId(website.loritta, it[TrackedYouTubeAccounts.youTubeChannelId])
                    }
                }.awaitAll().mapNotNull { (it as? YouTubeWebUtils.YouTubeChannelInfoResult.Success)?.channel }
                    .associateBy { it.channelId }

                call.respondHtmlFragment(status = HttpStatusCode.OK) {
                    trackedYouTubeChannelsSection(
                        i18nContext,
                        guild,
                        result.trackedYouTubeChannels.map {
                            val youtubeChannelInfo = youtubeChannelsInfo[it[TrackedYouTubeAccounts.youTubeChannelId]]

                            TrackedProfile(
                                youtubeChannelInfo?.name,
                                youtubeChannelInfo?.avatarUrl,
                                it[TrackedYouTubeAccounts.youTubeChannelId],
                                it[TrackedYouTubeAccounts.id].value,
                                it[TrackedYouTubeAccounts.channelId]
                            )
                        }
                    )

                    blissCloseModal()

                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Canal deletado!"
                        )
                    )
                }
            }
            Result.ChannelNotFound -> {
                call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você não pode deletar um canal que não existe!"
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