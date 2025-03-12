package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPunishmentMessagesConfig
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.ModerationConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.WarnAction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class ModerationConfigTransformer(val loritta: LorittaBot) : ConfigTransformer {
    override val payloadType: String = "moderation"
    override val configKey: String = "moderationConfig"

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        val moderationConfig = loritta.newSuspendedTransaction {
            serverConfig.moderationConfig
        }

        val actions = moderationConfig?.let {
            loritta.newSuspendedTransaction {
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

            val metadata = it.metadata?.let { JsonParser.parseString(it) }
            if (metadata != null)
                obj["customMetadata0"] = metadata["time"].string

            punishmentActions.add(obj)
        }

        val punishmentMessages = jsonArray()

        loritta.newSuspendedTransaction {
            ModerationPunishmentMessagesConfig.selectAll().where {
                ModerationPunishmentMessagesConfig.guild eq serverConfig.id
            }.toList()
        }.forEach {
            punishmentMessages.add(
                    jsonObject(
                            "action" to it[ModerationPunishmentMessagesConfig.punishmentAction].name,
                            "message" to it[ModerationPunishmentMessagesConfig.punishLogMessage]
                    )
            )
        }

        return jsonObject(
                "sendPunishmentViaDm" to (moderationConfig?.sendPunishmentViaDm ?: false),
                "sendPunishmentToPunishLog" to (moderationConfig?.sendPunishmentToPunishLog ?: false),
                "punishLogChannelId" to moderationConfig?.punishLogChannelId,
                "punishLogMessage" to (moderationConfig?.punishLogMessage ?: "**Usuário punido:** {user}#{user-discriminator}\n**Punido por** {@staff}\n**Motivo:** {reason}"),
                "punishmentActions" to punishmentActions,
                "punishmentMessages" to punishmentMessages
        )
    }

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        val sendPunishmentViaDm = payload["sendPunishmentViaDm"].bool
        val sendPunishmentToPunishLog = payload["sendPunishmentToPunishLog"].bool
        val punishLogChannelId = payload["punishLogChannelId"].nullLong
        val punishLogMessage = payload["punishLogMessage"].nullString
        val punishmentActions = payload["punishmentActions"].array
        val punishmentMessages = payload["punishmentMessages"].array

        loritta.newSuspendedTransaction {
            val moderationConfig = serverConfig.moderationConfig ?: ModerationConfig.new {
                this.sendPunishmentToPunishLog = false
                this.sendPunishmentViaDm = false
                this.punishLogMessage = null
                this.punishLogChannelId = null
            }

            WarnActions.deleteWhere {
                WarnActions.config eq moderationConfig.id
            }

            ModerationPunishmentMessagesConfig.deleteWhere {
                ModerationPunishmentMessagesConfig.guild eq serverConfig.id
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
                        ).toString()
                    }
                }
            }

            for (punishmentMessage in punishmentMessages.map { it.obj }) {
                val action = punishmentMessage["action"].string
                val message = punishmentMessage["message"].string

                ModerationPunishmentMessagesConfig.insert {
                    it[ModerationPunishmentMessagesConfig.guild] = serverConfig.id
                    it[ModerationPunishmentMessagesConfig.punishmentAction] = PunishmentAction.valueOf(action)
                    it[ModerationPunishmentMessagesConfig.punishLogMessage] = message
                }
            }
        }
    }
}