package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import dev.kord.common.entity.ChannelType
import dev.kord.gateway.ChannelCreate
import dev.kord.gateway.Event
import dev.kord.gateway.MessageReactionAdd
import dev.kord.gateway.MessageReactionRemove
import dev.kord.gateway.MessageReactionRemoveAll
import dev.kord.gateway.MessageReactionRemoveEmoji
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.StarboardModule
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
        private val json = Json { ignoreUnknownKeys = true }
    }

    override fun run() {
        while (true) {
            try {
                logger.debug { "Processing gateway events in the queue..." }

                val connection = pudding.hikariDataSource.connection
                connection.use {
                    val selectStatement =
                        it.prepareStatement("""SELECT id, "type", payload FROM ${DiscordGatewayEvents.tableName} ORDER BY id FOR UPDATE SKIP LOCKED LIMIT ${m.config.eventsPerBatch};""")
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
                        val discordEvent = json.decodeFromString(
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
                            // ===[ REACTIONS ]===
                            is MessageReactionAdd -> {
                                GlobalScope.launch {
                                    m.starboardModule.handleStarboardReaction(
                                        discordEvent.reaction.guildId.value ?: return@launch,
                                        discordEvent.reaction.channelId,
                                        discordEvent.reaction.messageId,
                                        discordEvent.reaction.emoji.name
                                    )
                                }
                            }
                            is MessageReactionRemove -> {
                                GlobalScope.launch {
                                    m.starboardModule.handleStarboardReaction(
                                        discordEvent.reaction.guildId.value ?: return@launch,
                                        discordEvent.reaction.channelId,
                                        discordEvent.reaction.messageId,
                                        discordEvent.reaction.emoji.name
                                    )
                                }
                            }
                            is MessageReactionRemoveEmoji -> {
                                GlobalScope.launch {
                                    m.starboardModule.handleStarboardReaction(
                                        discordEvent.reaction.guildId,
                                        discordEvent.reaction.channelId,
                                        discordEvent.reaction.messageId,
                                        discordEvent.reaction.emoji.name
                                    )
                                }
                            }
                            is MessageReactionRemoveAll -> {
                                GlobalScope.launch {
                                    m.starboardModule.handleStarboardReaction(
                                        discordEvent.reactions.guildId.value ?: return@launch,
                                        discordEvent.reactions.channelId,
                                        discordEvent.reactions.messageId,
                                        StarboardModule.STAR_REACTION // We only want the code to check if it should be removed from the starboard
                                    )
                                }
                            }

                            // ===[ CHANNEL CREATE ]===
                            is ChannelCreate -> {
                                // This should only be sent in a guild text channel
                                if (discordEvent.channel.type == ChannelType.GuildText) {
                                GlobalScope.launch {
                                        m.addFirstToNewChannelsModule.handleFirst(
                                            discordEvent.channel.guildId.value
                                                ?: return@launch, // Pretty sure that this cannot be null here
                                            discordEvent.channel.id
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }

                        count++

                        logger.debug { "Processed event $type (${discordEvent!!::class})" }
                        processedRows.add(id)
                    }

                    val deleteStatement = it.prepareStatement("DELETE FROM ${DiscordGatewayEvents.tableName} WHERE id = ANY(?)")
                    val array = connection.createArrayOf("bigint", processedRows.toTypedArray())
                    deleteStatement.setArray(1, array)
                    deleteStatement.execute()

                    it.commit()

                    logger.debug { "Successfully processed $count Discord gateway events! (${processedRows})" }
                }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while polling pending Discord gateway events!" }
            }
        }
    }
}