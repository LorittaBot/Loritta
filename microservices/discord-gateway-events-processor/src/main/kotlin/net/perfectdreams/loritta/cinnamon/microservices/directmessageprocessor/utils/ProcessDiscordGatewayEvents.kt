package net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Event
import dev.kord.gateway.MessageReactionAdd
import dev.kord.gateway.MessageReactionRemove
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.DiscordGatewayEvents

/**
 * Processes correios pending messages from our message queue.
 */
class ProcessDiscordGatewayEvents(
    private val m: DiscordGatewayEventsProcessor,
    private val pudding: Pudding
) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        while (true) {
            try {
                // logger.debug { "Processing gateway events in the queue..." }

                val connection = pudding.hikariDataSource.connection
                connection.use {
                    val selectStatement =
                        it.prepareStatement("""SELECT id, "type", payload FROM ${DiscordGatewayEvents.tableName} ORDER BY id FOR UPDATE SKIP LOCKED LIMIT 10;""")
                    val rs = selectStatement.executeQuery()

                    var count = 0
                    val processedRows = mutableListOf<Long>()

                    while (rs.next()) {
                        val id = rs.getLong("id")
                        val type = rs.getString("type")
                        val gatewayPayload = rs.getString("payload")

                        // TODO: Ktor doesn't deserialize this well because it relies on the order
                        val gatewayPayloadStuff = Json.parseToJsonElement(gatewayPayload)
                            .jsonObject

                        // Using decodeFromJsonElement crashes with "Index -1 out of bounds for length 0", why?
                        val discordEvent = Json { ignoreUnknownKeys = true }.decodeFromString(
                            Event.DeserializationStrategy,
                            buildJsonObject {
                                gatewayPayloadStuff["op"]?.let {
                                    put("op", it.jsonPrimitive.longOrNull)
                                }

                                gatewayPayloadStuff["t"]?.let {
                                    put("t", it)
                                }

                                gatewayPayloadStuff["s"]?.let {
                                    put("s", it)
                                }

                                gatewayPayloadStuff["d"]?.let {
                                    put("d", it)
                                }
                            }.toString()
                        )

                        when (discordEvent) {
                            is MessageReactionAdd -> {
                                logger.info { "Someone added a reaction! ${discordEvent.reaction.userId} ${discordEvent.reaction.emoji} ${discordEvent.reaction.messageId}" }
                                // TODO: Don't use GlobalScope
                                if (discordEvent.reaction.guildId.value == Snowflake(268353819409252352L)) {
                                    repeat(5) {
                                        GlobalScope.launch {
                                            // test crash
                                            m.starboardModule.handleStarboardReaction(
                                                discordEvent.reaction.channelId,
                                                discordEvent.reaction.guildId.value ?: return@launch,
                                                discordEvent.reaction.messageId,
                                                discordEvent.reaction.emoji
                                            )
                                        }
                                    }
                                } else {
                                    GlobalScope.launch {
                                        // test crash
                                        m.starboardModule.handleStarboardReaction(
                                            discordEvent.reaction.channelId,
                                            discordEvent.reaction.guildId.value ?: return@launch,
                                            discordEvent.reaction.messageId,
                                            discordEvent.reaction.emoji
                                        )
                                    }
                                }
                            }
                            is MessageReactionRemove -> {
                                logger.info { "Someone removed a reaction! ${discordEvent.reaction.userId} ${discordEvent.reaction.emoji} ${discordEvent.reaction.messageId}" }
                                GlobalScope.launch {
                                    m.starboardModule.handleStarboardReaction(
                                        discordEvent.reaction.channelId,
                                        discordEvent.reaction.guildId.value ?: return@launch,
                                        discordEvent.reaction.messageId,
                                        discordEvent.reaction.emoji
                                    )
                                }
                            }
                            else -> {}
                        }

                        count++

                        // logger.info { "Processed event $type (${discordEvent!!::class})" }
                        processedRows.add(id)
                    }

                    val deleteStatement = it.prepareStatement("DELETE FROM ${DiscordGatewayEvents.tableName} WHERE id = ANY(?)")
                    val array = connection.createArrayOf("bigint", processedRows.toTypedArray())
                    deleteStatement.setArray(1, array)
                    deleteStatement.execute()

                    it.commit()

                    // logger.info { "Successfully processed $count Discord gateway events! (${processedRows})" }
                }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while polling pending Discord gateway events!" }
            }
        }
    }
}