package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import dev.kord.common.entity.ChannelType
import dev.kord.gateway.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.StarboardModule

/**
 * Processes correios pending messages from our message queue.
 */
class ProcessDiscordGatewayEvents(
    private val m: DiscordGatewayEventsProcessor,
    private val channel: Channel
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val json = Json { ignoreUnknownKeys = true }
    }

    init {
        val factory = ConnectionFactory()
        factory.host = "localhost"

        val connection = factory.newConnection()
        val channel1 = connection.createChannel()
    }

    fun setupConsumer() {
        channel.basicConsume(
            "loritta.discord-gateway-events",
            true,
            DeliverCallback { consumerTag, message ->
                val gatewayPayload = message.body.toString(Charsets.UTF_8)

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

                logger.debug { "Processed event ${discordEvent!!::class}" }
            },
            CancelCallback {

            }
        )
    }
}