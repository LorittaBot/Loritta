package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.tables.TrackedTwitterAccounts
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object TwitterConfigTransformer : ConfigTransformer {
    override val payloadType: String = "twitter"
    override val configKey: String = "trackedTwitterAccounts"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        transaction(Databases.loritta) {
            TrackedTwitterAccounts.deleteWhere {
                TrackedTwitterAccounts.guildId eq guild.idLong
            }

            val accounts = payload["accounts"].array

            for (account in accounts) {
                TrackedTwitterAccounts.insert {
                    it[guildId] = guild.idLong
                    it[channelId] = account["channel"].long
                    it[twitterAccountId] = account["twitterAccountId"].long
                    it[message] = account["message"].string
                }
            }
        }

        lorittaShards.queryMasterLorittaCluster("/api/v1/twitter/update-stream")
    }

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return transaction(Databases.loritta) {
            val array = JsonArray()

            TrackedTwitterAccounts.select {
                TrackedTwitterAccounts.guildId eq guild.idLong
            }.forEach {
                array.add(
                        jsonObject(
                                "channelId" to it[TrackedTwitterAccounts.channelId],
                                "twitterAccountId" to it[TrackedTwitterAccounts.twitterAccountId],
                                "message" to it[TrackedTwitterAccounts.message]
                        )
                )
            }

            array
        }
    }
}