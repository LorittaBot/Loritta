package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.drops

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordInviteUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DropsConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.upsert
import java.time.OffsetDateTime

class PutDropsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/drops") {
    @Serializable
    data class SaveDropsRequest(
        val showGuildInformationOnTransactions: Boolean,
        val inviteId: String?
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SaveDropsRequest>(call.receiveText())
        if (request.showGuildInformationOnTransactions && !guildPremiumPlan.showDropGuildInfoOnTransactions) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "O servidor precisa ter premium para fazer isto!"
                    )
                )
            }
            return
        }

        val inviteCode = if (request.inviteId != null) {
            val inviteCode = if (request.inviteId.contains("/")) {
                DiscordInviteUtils.getInviteCodeFromUrl(request.inviteId)
            } else {
                request.inviteId
            }

            if (inviteCode == null || !DiscordInviteUtils.inviteCodeRegex.matches(inviteCode)) {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            i18nContext.get(DashboardI18nKeysData.Drops.ShowGuildInformationOnTransactions.InvalidInviteUrl)
                        )
                    )
                }
                return
            }

            try {
                val invite = Invite.resolve(guild.jda, inviteCode, false)
                    .submit(false)
                    .await()

                invite.code
            } catch (e: Exception) {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            i18nContext.get(DashboardI18nKeysData.Drops.ShowGuildInformationOnTransactions.InvalidInvite)
                        )
                    )
                }
                return
            }
        } else {
            null
        }

        website.loritta.transaction {
            DropsConfigs.upsert(DropsConfigs.id) {
                it[DropsConfigs.id] = guild.idLong
                it[DropsConfigs.showGuildInformationOnTransactions] = request.showGuildInformationOnTransactions
                it[DropsConfigs.guildInviteCode] = inviteCode
                it[DropsConfigs.updatedAt] = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}