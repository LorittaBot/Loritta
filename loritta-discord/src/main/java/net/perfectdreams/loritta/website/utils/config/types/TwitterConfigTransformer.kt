package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.array
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.serializable.TrackedTwitterAccount
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedTwitterAccounts
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

object TwitterConfigTransformer : ConfigTransformer {
    override val payloadType: String = "twitter"
    override val configKey: String = "trackedTwitterAccounts"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        loritta.newSuspendedTransaction {
            TrackedTwitterAccounts.deleteWhere {
                TrackedTwitterAccounts.guildId eq guild.idLong
            }

            val accounts = Json.decodeFromString(ListSerializer(TrackedTwitterAccount.serializer()), payload["accounts"].array.toString())

            for (account in accounts) {
                TrackedTwitterAccounts.insert {
                    it[guildId] = guild.idLong
                    it[channelId] = account.channelId
                    it[twitterAccountId] = account.twitterAccountId
                    it[message] = account.message
                }
            }
        }
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
                    Json.encodeToString(ListSerializer(TrackedTwitterAccount.serializer()), trackedTwitterAccounts)
            )
        }
    }
}