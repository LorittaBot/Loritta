package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.array
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.serializable.TrackedTwitterAccount
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedTwitterAccounts
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object TwitterConfigTransformer : ConfigTransformer {
    override val payloadType: String = "twitter"
    override val configKey: String = "trackedTwitterAccounts"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        loritta.newSuspendedTransaction {
            TrackedTwitterAccounts.deleteWhere {
                TrackedTwitterAccounts.guildId eq guild.idLong
            }

            val accounts = Json.parse(TrackedTwitterAccount.serializer().list, payload["accounts"].array.toString())

            for (account in accounts) {
                TrackedTwitterAccounts.insert {
                    it[guildId] = guild.idLong
                    it[channelId] = account.channelId
                    it[twitterAccountId] = account.twitterAccountId
                    it[message] = account.message
                }
            }
        }

        lorittaShards.queryMasterLorittaCluster("/api/v1/twitter/update-stream")
    }

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return loritta.newSuspendedTransaction {
            val trackedTwitterAccounts = TrackedTwitterAccounts.select {
                TrackedTwitterAccounts.guildId eq guild.idLong
            }.map {
                TrackedTwitterAccount(
                        it[TrackedTwitterAccounts.channelId],
                        it[TrackedTwitterAccounts.twitterAccountId],
                        it[TrackedTwitterAccounts.message]
                )
            }

            JsonParser.parseString(
                    Json.stringify(TrackedTwitterAccount.serializer().list, trackedTwitterAccounts)
            )
        }
    }
}