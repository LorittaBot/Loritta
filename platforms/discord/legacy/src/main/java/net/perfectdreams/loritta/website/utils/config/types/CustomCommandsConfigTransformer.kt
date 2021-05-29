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
import net.perfectdreams.loritta.serializable.CustomCommand
import net.perfectdreams.loritta.tables.servers.CustomGuildCommands
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

object CustomCommandsConfigTransformer : ConfigTransformer {
    override val payloadType: String = "custom_commands"
    override val configKey: String = "customCommands"

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        val customCommands = loritta.newSuspendedTransaction {
            CustomGuildCommands.select {
                CustomGuildCommands.guild eq serverConfig.id.value
            }.map {
                CustomCommand(
                        it[CustomGuildCommands.label],
                        it[CustomGuildCommands.codeType],
                        it[CustomGuildCommands.code]
                )
            }
        }

        return JsonParser.parseString(Json.encodeToString(ListSerializer(CustomCommand.serializer()), customCommands))
    }

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        loritta.newSuspendedTransaction {
            // First we delete all of them...
            CustomGuildCommands.deleteWhere {
                CustomGuildCommands.guild eq serverConfig.id
            }

            // And now we reinsert the new commands
            val entries = Json.decodeFromString(ListSerializer(CustomCommand.serializer()), payload["entries"].array.toString())

            for (entry in entries) {
                CustomGuildCommands.insert {
                    it[CustomGuildCommands.guild] = serverConfig.id
                    it[CustomGuildCommands.enabled] = true
                    it[CustomGuildCommands.label] = entry.label
                    it[CustomGuildCommands.codeType] = entry.codeType
                    it[CustomGuildCommands.code] = entry.code
                }
            }
        }
    }
}