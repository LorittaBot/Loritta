package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.drops

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.future.await
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.style
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordInviteUtils
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings

class PostDropsInviteGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/drops/invite") {
    @Serializable
    data class ValidateDropInviteRequest(
        val inviteId: String?
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<ValidateDropInviteRequest>(call.receiveText())
        if (request.inviteId == null) {
            call.respondHtmlFragment {
                div {
                    text(i18nContext.get(DashboardI18nKeysData.Drops.ShowGuildInformationOnTransactions.NoInviteConfigured))
                }
            }
            return
        }

        val inviteCode = if (request.inviteId.contains("/")) {
            DiscordInviteUtils.getInviteCodeFromUrl(request.inviteId)
        } else {
            request.inviteId
        }

        if (inviteCode == null || !DiscordInviteUtils.inviteCodeRegex.matches(inviteCode)) {
            call.respondHtmlFragment {
                div {
                    text(i18nContext.get(DashboardI18nKeysData.Drops.ShowGuildInformationOnTransactions.InvalidInviteUrl))
                }
            }
            return
        }

        val invite = try {
            Invite.resolve(guild.jda, inviteCode, false)
                .submit(false)
                .await()
        } catch (e: Exception) {
            call.respondHtmlFragment {
                div {
                    text(i18nContext.get(DashboardI18nKeysData.Drops.ShowGuildInformationOnTransactions.InvalidInvite))
                }
            }
            return
        }

        call.respondHtmlFragment {
            div(classes = "cards") {
                div(classes = "card") {
                    style = "flex-direction: row; align-items: center; gap: 0.5em;"

                    div {
                        style = "flex-grow: 1; display: flex; gap: 0.5em; align-items: center;"

                        img(src = invite.guild?.iconUrl) {
                           style = "width: 48px; height: 48px; border-radius: 25%;"
                        }

                        div {
                            style = "display: flex; flex-direction: column;"
                            b {
                                text("${invite.guild?.name}")
                            }

                            div {
                                text("${invite.guild?.idLong}")
                            }
                        }
                    }
                }
            }
        }
    }
}