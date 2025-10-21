package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.warnactions

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.services.ModerationLogsService
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.ModerationConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class PutWarnActionsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/warn-actions") {
    @Serializable
    data class SaveWarnActionsRequest(
        val actions: List<WarnAction> = listOf()
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val request = Json.decodeFromString<SaveWarnActionsRequest>(call.receiveText())

        website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val moderationConfig = serverConfig.moderationConfig ?: ModerationConfig.new {
                this.sendPunishmentToPunishLog = false
                this.sendPunishmentViaDm = false
                this.punishLogMessage = null
                this.punishLogChannelId = null
            }

            WarnActions.deleteWhere {
                WarnActions.config eq moderationConfig.id
            }

            for (punishmentAction in request.actions) {
                val time = punishmentAction.time

                WarnActions.insert {
                    it[WarnActions.config] = moderationConfig.id
                    it[WarnActions.punishmentAction] = punishmentAction.action
                    it[WarnActions.warnCount] = punishmentAction.count
                    if (punishmentAction.action == PunishmentAction.MUTE && time != null) {
                        it[WarnActions.metadata] = buildJsonObject {
                            put("time", time)
                        }.toString()
                    }
                }
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}