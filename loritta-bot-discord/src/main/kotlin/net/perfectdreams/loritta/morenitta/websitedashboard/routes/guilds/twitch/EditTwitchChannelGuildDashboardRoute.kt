package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.div
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch.TwitchWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class EditTwitchChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/twitch/{entryId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val data = website.loritta.transaction {
            TrackedTwitchAccounts.selectAll()
                .where {
                    TrackedTwitchAccounts.id eq entryId and (TrackedTwitchAccounts.guildId eq guild.idLong)
                }
                .firstOrNull()
        }

        if (data == null) {
            // TODO - bliss-dash: Add a proper page!
            call.respond(HttpStatusCode.NotFound)
            return
        }
        
        val twitchAccountTrackingState = website.loritta.transaction {
            TwitchWebUtils.getTwitchAccountTrackState(data[TrackedTwitchAccounts.twitchUserId])
        }

        val twitchUser = TwitchWebUtils.getCachedUsersInfoById(website.loritta, data[TrackedTwitchAccounts.twitchUserId])
            .first()

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.Twitch.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.TWITCH)
                        },
                        {
                            goBackToPreviousSectionButton(
                                href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch",
                            ) {
                                text("Voltar para a lista de contas da Twitch")
                            }

                            hr {}

                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    trackedProfileHeader(twitchUser.displayName, twitchUser.profileImageUrl)

                                    when (twitchAccountTrackingState) {
                                        TwitchAccountTrackState.AUTHORIZED -> {
                                            div(classes = "alert alert-success") {
                                                text("O canal foi autorizado pelo dono, então você receberá notificações quando o canal entrar ao vivo!")
                                            }
                                        }
                                        TwitchAccountTrackState.ALWAYS_TRACK_USER -> {
                                            div(classes = "alert alert-success") {
                                                text("O canal não está autorizado, mas ela está na minha lista especial de \"pessoas tão incríveis que não preciso pedir autorização\". Você receberá notificações quando o canal entrar ao vivo.")
                                            }
                                        }
                                        TwitchAccountTrackState.PREMIUM_TRACK_USER -> {
                                            div(classes = "alert alert-success") {
                                                text("O canal não está autorizado, mas você colocou ele na lista de acompanhamentos premium! Você receberá notificações quando o canal entrar ao vivo.")
                                            }
                                        }
                                        TwitchAccountTrackState.UNAUTHORIZED -> {
                                            div(classes = "alert alert-danger") {
                                                text("O canal não está autorizado! Você só receberá notificações quando o canal for autorizado na Loritta.")
                                            }
                                        }
                                    }

                                    sectionConfig {
                                        trackedTwitchChannelEditor(
                                            i18nContext,
                                            guild,
                                            data[TrackedTwitchAccounts.channelId],
                                            data[TrackedTwitchAccounts.message]
                                        )
                                    }
                                },
                                {
                                    trackedProfileEditorSaveBar(
                                        i18nContext,
                                        guild,
                                        "twitch",
                                        data[TrackedTwitchAccounts.id].value
                                    )
                                }
                            )
                        }
                    )
                }
        )
    }
}