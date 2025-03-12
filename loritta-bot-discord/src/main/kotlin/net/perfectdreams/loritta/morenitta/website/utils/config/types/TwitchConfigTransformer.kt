package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class TwitchConfigTransformer(val loritta: LorittaBot) : ConfigTransformer {
    override val payloadType: String = "twitch"
    override val configKey: String = "trackedTwitchChannels"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        loritta.newSuspendedTransaction {
            TrackedTwitchAccounts.deleteWhere {
                TrackedTwitchAccounts.guildId eq guild.idLong
            }

            val accounts = payload["accounts"].array

            for (account in accounts) {
                TrackedTwitchAccounts.insert {
                    it[guildId] = guild.idLong
                    it[channelId] = account["channel"].long
                    it[twitchUserId] = account["twitchUserId"].long
                    it[message] = account["message"].string
                }
            }
        }
    }

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return loritta.newSuspendedTransaction {
            val array = JsonArray()

            TrackedTwitchAccounts.selectAll().where {
                TrackedTwitchAccounts.guildId eq guild.idLong
            }.forEach {
                array.add(
                        jsonObject(
                                "channelId" to it[TrackedTwitchAccounts.channelId],
                                "twitchUserId" to it[TrackedTwitchAccounts.twitchUserId],
                                "message" to it[TrackedTwitchAccounts.message],
                                "webhookUrl" to it[TrackedTwitchAccounts.webhookUrl]
                        )
                )
            }

            array
        }
    }
}