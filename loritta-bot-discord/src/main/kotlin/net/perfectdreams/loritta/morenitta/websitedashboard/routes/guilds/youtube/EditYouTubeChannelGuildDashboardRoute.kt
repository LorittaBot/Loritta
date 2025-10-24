package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.body
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class EditYouTubeChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/youtube/{entryId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val data = website.loritta.transaction {
            TrackedYouTubeAccounts.selectAll()
                .where {
                    TrackedYouTubeAccounts.id eq entryId and (TrackedYouTubeAccounts.guildId eq guild.idLong)
                }
                .firstOrNull()
        }

        if (data == null) {
            // TODO - bliss-dash: Add a proper page!
            call.respond(HttpStatusCode.NotFound)
            return
        }

        val result = YouTubeWebUtils.getYouTubeChannelInfoFromChannelId(website.loritta, data[TrackedYouTubeAccounts.youTubeChannelId])

        when (result) {
            is YouTubeWebUtils.YouTubeChannelInfoResult.Success -> {
                call.respondHtml(
                    createHTML()
                        .html {
                            dashboardBase(
                                i18nContext,
                                i18nContext.get(DashboardI18nKeysData.CustomCommands.Title),
                                session,
                                theme,
                                shimejiSettings,
                                userPremiumPlan,
                                {
                                    guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.YOUTUBE)
                                },
                                {
                                    goBackToPreviousSectionButton(
                                        href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/youtube",
                                    ) {
                                        text("Voltar para a lista de canais do YouTube")
                                    }

                                    hr {}

                                    rightSidebarContentAndSaveBarWrapper(
                                        {
                                            trackedYouTubeChannelEditorWithProfile(
                                                i18nContext,
                                                guild,
                                                result.channel,
                                                data[TrackedYouTubeAccounts.channelId],
                                                data[TrackedYouTubeAccounts.message]
                                            )
                                        },
                                        {
                                            trackedProfileEditorSaveBar(
                                                i18nContext,
                                                guild,
                                                "youtube",
                                                data[TrackedYouTubeAccounts.id].value
                                            )
                                        }
                                    )
                                }
                            )
                        }
                )
            }
            is YouTubeWebUtils.YouTubeChannelInfoResult.Error -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "Algo deu errado ao tentar pegar as informações do canal!"
                                )
                            )
                        },
                    status = HttpStatusCode.BadRequest
                )
            }
            YouTubeWebUtils.YouTubeChannelInfoResult.InvalidUrl -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "URL inválida!"
                                )
                            )
                        },
                    status = HttpStatusCode.BadRequest
                )
            }
            YouTubeWebUtils.YouTubeChannelInfoResult.UnknownChannel -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "Canal não existe!"
                                )
                            )
                        },
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }
}