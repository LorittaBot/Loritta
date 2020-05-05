package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.servers.moduleconfigs.ModerationConfig
import net.perfectdreams.loritta.dao.servers.moduleconfigs.WarnAction
import net.perfectdreams.loritta.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object ModerationConfigTransformer : ConfigTransformer {
    override val payloadType: String = "moderation"
    override val configKey: String = "moderationConfig"

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        val moderationConfig = transaction(Databases.loritta) {
            serverConfig.moderationConfig
        }

        val actions = moderationConfig?.let {
            transaction(Databases.loritta) {
                WarnAction.find {
                    WarnActions.config eq it.id
                }.toList()
            }
        } ?: listOf()

        val punishmentActions = jsonArray()

        actions.forEach {
            val obj = jsonObject(
                    "warnCount" to it.warnCount,
                    "punishmentAction" to it.punishmentAction.name
            )

            val metadata = it.metadata
            if (metadata != null)
                obj["customMetadata0"] = metadata["time"].string

            punishmentActions.add(obj)
        }

        return jsonObject(
                "sendPunishmentViaDm" to (moderationConfig?.sendPunishmentViaDm ?: false),
                "sendPunishmentToPunishLog" to (moderationConfig?.sendPunishmentToPunishLog ?: false),
                "punishLogChannelId" to moderationConfig?.punishLogChannelId,
                "punishLogMessage" to (moderationConfig?.punishLogMessage ?: "**Usuário punido:** {user}#{user-discriminator}\n**Punido por** {@staff}\n**Motivo:** {reason}"),
                "punishmentActions" to punishmentActions
        )
    }

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        val sendPunishmentViaDm = payload["sendPunishmentViaDm"].bool
        val sendPunishmentToPunishLog = payload["sendPunishmentToPunishLog"].bool
        val punishLogChannelId = payload["punishLogChannelId"].nullLong
        val punishLogMessage = payload["punishLogMessage"].nullString
        val punishmentActions = payload["punishmentActions"].array

        transaction(Databases.loritta) {
            val moderationConfig = serverConfig.moderationConfig ?: ModerationConfig.new {
                this.sendPunishmentToPunishLog = false
                this.sendPunishmentViaDm = false
                this.punishLogMessage = null
                this.punishLogChannelId = null
            }

            WarnActions.deleteWhere {
                WarnActions.config eq moderationConfig.id
            }

            moderationConfig.sendPunishmentViaDm = sendPunishmentViaDm
            moderationConfig.sendPunishmentToPunishLog = sendPunishmentToPunishLog
            if (sendPunishmentToPunishLog) {
                moderationConfig.punishLogChannelId = punishLogChannelId
                moderationConfig.punishLogMessage = punishLogMessage
            } else {
                moderationConfig.punishLogChannelId = null
                moderationConfig.punishLogMessage = null
            }

            serverConfig.moderationConfig = moderationConfig

            for (punishmentAction in punishmentActions.map { it.obj }) {
                val action = punishmentAction["punishmentAction"].string
                val warnCount = punishmentAction["warnCount"].int
                val time = punishmentAction["time"].nullString

                WarnActions.insert {
                    it[WarnActions.config] = moderationConfig.id
                    it[WarnActions.punishmentAction] = PunishmentAction.valueOf(action)
                    it[WarnActions.warnCount] = warnCount
                    if (time != null) {
                        it[WarnActions.metadata] = jsonObject(
                                "time" to time
                        )
                    }
                }
            }
        }
    }
}