package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.github.salomonbrys.kotson.array
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitterAccounts
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.serializable.TrackedTwitterAccount
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class TwitterConfigTransformer(val loritta: LorittaBot) : ConfigTransformer {
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
            val trackedTwitterAccounts = TrackedTwitterAccounts.selectAll().where {
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