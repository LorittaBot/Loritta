package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.servers.moduleconfigs.WelcomerConfig

object WelcomerConfigTransformer : ConfigTransformer {
    override val payloadType: String = "welcomer"
    override val configKey: String = "welcomerConfig"

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        val welcomerConfig = loritta.newSuspendedTransaction {
            serverConfig.welcomerConfig
        }

        return jsonObject(
                "enabled" to (welcomerConfig != null),
                "tellOnJoin" to (welcomerConfig?.tellOnJoin ?: false),
                "tellOnRemove" to (welcomerConfig?.tellOnRemove ?: false),
                "joinMessage" to welcomerConfig?.joinMessage,
                "removeMessage" to welcomerConfig?.removeMessage,
                "channelJoinId" to welcomerConfig?.channelJoinId,
                "channelRemoveId" to welcomerConfig?.channelRemoveId,
                "tellOnPrivateJoin" to (welcomerConfig?.tellOnPrivateJoin ?: false),
                "tellOnBan" to (welcomerConfig?.tellOnBan ?: false),
                "bannedMessage" to welcomerConfig?.bannedMessage,
                "deleteJoinMesagesAfter" to welcomerConfig?.deleteJoinMessagesAfter,
                "deleteRemoveMessagesAfter" to welcomerConfig?.deleteRemoveMessagesAfter
        )
    }

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        val isEnabled = payload["isEnabled"].bool
        val tellOnJoin = payload["tellOnJoin"].bool
        val channelJoinId = payload["channelJoinId"].long
        val joinMessage = payload["joinMessage"].nullString
        val deleteJoinMessagesAfter = payload["deleteJoinMessagesAfter"].nullLong

        val tellOnRemove = payload["tellOnRemove"].bool
        val channelRemoveId = payload["channelRemoveId"].long
        val removeMessage = payload["removeMessage"].nullString
        val deleteRemoveMessagesAfter = payload["deleteRemoveMessagesAfter"].nullLong

        val tellOnBan = payload["tellOnBan"].bool
        val banMessage = payload["bannedMessage"].nullString

        val tellOnPrivate = payload["tellOnPrivateJoin"].bool
        val joinPrivateMessage = payload["joinPrivateMessage"].nullString

        loritta.newSuspendedTransaction {
            val welcomerConfig = serverConfig.welcomerConfig

            if (!isEnabled) {
                serverConfig.welcomerConfig = null
                welcomerConfig?.delete()
            } else {
                val newConfig = welcomerConfig ?: WelcomerConfig.new {
                    this.tellOnJoin = tellOnJoin
                    this.channelJoinId = channelJoinId
                    this.joinMessage = joinMessage
                    this.deleteJoinMessagesAfter = deleteJoinMessagesAfter
                    this.tellOnRemove = tellOnRemove
                    this.channelRemoveId = channelRemoveId
                    this.removeMessage = removeMessage
                    this.deleteRemoveMessagesAfter = deleteRemoveMessagesAfter
                    this.tellOnBan = tellOnBan
                    this.bannedMessage = banMessage
                    this.tellOnPrivateJoin = tellOnPrivate
                    this.joinPrivateMessage = joinPrivateMessage
                }

                newConfig.tellOnJoin = tellOnJoin
                newConfig.channelJoinId = channelJoinId
                newConfig.joinMessage = joinMessage
                newConfig.deleteJoinMessagesAfter = deleteJoinMessagesAfter
                newConfig.tellOnRemove = tellOnRemove
                newConfig.channelRemoveId = channelRemoveId
                newConfig.removeMessage = removeMessage
                newConfig.deleteRemoveMessagesAfter = deleteRemoveMessagesAfter
                newConfig.tellOnBan = tellOnBan
                newConfig.bannedMessage = banMessage
                newConfig.tellOnPrivateJoin = tellOnPrivate
                newConfig.joinPrivateMessage = joinPrivateMessage

                serverConfig.welcomerConfig = newConfig
            }
        }
    }
}