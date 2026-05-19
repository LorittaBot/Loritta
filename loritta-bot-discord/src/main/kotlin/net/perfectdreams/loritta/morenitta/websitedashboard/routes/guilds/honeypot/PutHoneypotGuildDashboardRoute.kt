package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.honeypot

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.HoneypotConfigs
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.TrackedChangeType
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.WebAuditLogUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.upsert

class PutHoneypotGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/honeypot") {
    @Serializable
    data class SaveHoneypotRequest(
        val enabled: Boolean,
        val action: String,
        val deleteDays: Int,
        val reason: String? = null,
        val channels: Set<Long> = setOf()
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
        val request = Json.decodeFromString<SaveHoneypotRequest>(call.receiveText())

        val parsedAction = when (request.action) {
            PunishmentAction.BAN.name -> PunishmentAction.BAN
            PunishmentAction.KICK.name -> PunishmentAction.KICK
            else -> PunishmentAction.PURGE_KICK
        }
        val coercedDeleteDays = request.deleteDays.coerceIn(0, 7)
        val sanitizedReason = request.reason?.takeIf { it.isNotBlank() }

        website.loritta.transaction {
            HoneypotConfigs.upsert(HoneypotConfigs.id) {
                it[HoneypotConfigs.id] = guild.idLong
                it[HoneypotConfigs.enabled] = request.enabled
                it[HoneypotConfigs.action] = parsedAction
                it[HoneypotConfigs.deleteDays] = coercedDeleteDays
                it[HoneypotConfigs.reason] = sanitizedReason
                it[HoneypotConfigs.channels] = request.channels.toList()
            }

            WebAuditLogUtils.addEntry(
                guild.idLong,
                session.userId,
                call.request.trueIp,
                call.request.userAgent(),
                TrackedChangeType.CHANGED_HONEYPOT
            )
        }

        call.respondConfigSaved(i18nContext)
    }
}
