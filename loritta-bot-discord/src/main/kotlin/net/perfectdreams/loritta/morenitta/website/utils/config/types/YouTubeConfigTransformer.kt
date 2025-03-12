package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class YouTubeConfigTransformer(val loritta: LorittaBot) : ConfigTransformer {
    override val payloadType: String = "youtube"
    override val configKey: String = "trackedYouTubeChannels"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        loritta.newSuspendedTransaction {
            TrackedYouTubeAccounts.deleteWhere {
                TrackedYouTubeAccounts.guildId eq guild.idLong
            }

            val accounts = payload["accounts"].array

            for (account in accounts) {
                TrackedYouTubeAccounts.insert {
                    it[guildId] = guild.idLong
                    it[channelId] = account["channel"].long
                    it[youTubeChannelId] = account["youTubeChannelId"].string
                    it[message] = account["message"].string
                }
            }
        }
    }

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return loritta.newSuspendedTransaction {
            val array = JsonArray()

            TrackedYouTubeAccounts.selectAll().where {
                TrackedYouTubeAccounts.guildId eq guild.idLong
            }.forEach {
                array.add(
                        jsonObject(
                                "channelId" to it[TrackedYouTubeAccounts.channelId],
                                "youTubeChannelId" to it[TrackedYouTubeAccounts.youTubeChannelId],
                                "message" to it[TrackedYouTubeAccounts.message],
                                "webhookUrl" to it[TrackedYouTubeAccounts.webhookUrl]
                        )
                )
            }

            array
        }
    }
}