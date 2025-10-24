package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch.TwitchWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.TrackedProfile
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedTwitchChannelsSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.switchtwitch.data.TwitchUser
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import kotlin.collections.map

class DeleteTwitchChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/twitch/{channelId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val channelId = call.parameters.getOrFail("channelId").toLong()

        val result = website.loritta.transaction {
            val deletedCount = TrackedTwitchAccounts.deleteWhere {
                TrackedTwitchAccounts.guildId eq guild.idLong and (TrackedTwitchAccounts.id eq channelId)
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
                val trackedProfiles = website.loritta.transaction {
                    TrackedTwitchAccounts.selectAll()
                        .where {
                            TrackedTwitchAccounts.guildId eq guild.idLong
                        }
                        .toList()
                }

                val profilesInfo = mutableMapOf<Long, TwitchUser>()
                if (trackedProfiles.isNotEmpty()) {
                    val accountsInfo = TwitchWebUtils.getCachedUsersInfoById(
                        website.loritta,
                        *trackedProfiles.map { it[TrackedTwitchAccounts.twitchUserId] }.toLongArray()
                    )

                    profilesInfo.putAll(accountsInfo.associateBy { it.id })
                }

                call.respondHtml(
                    createHTML(false)
                        .body {
                            trackedTwitchChannelsSection(
                                website.loritta,
                                i18nContext,
                                guild,
                                trackedProfiles.map {
                                    val profileInfo = profilesInfo[it[TrackedTwitchAccounts.id].value]

                                    TrackedProfile(
                                        profileInfo?.login,
                                        profileInfo?.profileImageUrl,
                                        it[TrackedTwitchAccounts.twitchUserId].toString(),
                                        it[TrackedTwitchAccounts.id].value,
                                        it[TrackedTwitchAccounts.channelId]
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
                        },
                    status = HttpStatusCode.OK
                )
            }
            Result.ChannelNotFound -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "Você não pode deletar um canal que não existe!"
                                )
                            )
                        },
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }

    private sealed class Result {
        data class Success(val trackedYouTubeChannels: List<ResultRow>) : Result()
        data object ChannelNotFound : Result()
    }
}