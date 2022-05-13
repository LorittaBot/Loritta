package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import com.rabbitmq.client.Channel
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.ChannelCreate
import dev.kord.gateway.Event
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor

class AddFirstToNewChannelsModule(private val m: DiscordGatewayEventsProcessor) : ProcessDiscordEventsModule(RABBITMQ_QUEUE) {
    companion object {
        const val RABBITMQ_QUEUE = "first-on-new-channels-module"

        private val FUNNY_FIRST_EMOJIS = listOf(
            Emotes.LoriCoffee,
            Emotes.LoriHappy,
            Emotes.LoriSmile,
            Emotes.LoriSunglasses,
            Emotes.LoriUwU,
            Emotes.LoriWow,
            Emotes.LoriStonks,
            Emotes.LoriKiss,
            Emotes.LoriLick,
            Emotes.LoriFlushed
        )
    }

    override fun setupQueueBinds(channel: Channel) {
        channel.queueBindToModuleQueue("event.channel-create")
    }

    override suspend fun processEvent(event: Event) {
        when (event) {
            // ===[ CHANNEL CREATE ]===
            is ChannelCreate -> {
                // This should only be sent in a guild text channel
                if (event.channel.type == ChannelType.GuildText) {
                    handleFirst(
                        event.channel.guildId.value ?: return, // Pretty sure that this cannot be null here
                        event.channel.id
                    )
                }
            }
            else -> {}
        }
    }

    suspend fun handleFirst(
        guildId: Snowflake,
        channelId: Snowflake
    ) {
        val serverConfig = m.services.serverConfigs.getServerConfigRoot(guildId.value) ?: return
        val miscellaneousConfig = serverConfig.getMiscellaneousConfig() ?: return

        if (!miscellaneousConfig.enableQuirky)
            return

        m.rest.channel.createMessage(channelId) {
            content = "First! ${FUNNY_FIRST_EMOJIS.random()}"
        }
    }
}