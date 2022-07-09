package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.ChannelCreate
import dev.kord.gateway.Event
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor

class AddFirstToNewChannelsModule(private val m: DiscordGatewayEventsProcessor) : ProcessDiscordEventsModule() {
    companion object {
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

    override suspend fun processEvent(shardId: Int, event: Event): ModuleResult {
        when (event) {
            // ===[ CHANNEL CREATE ]===
            is ChannelCreate -> {
                // This should only be sent in a guild text channel
                if (event.channel.type == ChannelType.GuildText) {
                    handleFirst(
                        event.channel.guildId.value ?: return ModuleResult.Continue, // Pretty sure that this cannot be null here
                        event.channel.id
                    )
                }
            }
            else -> {}
        }
        return ModuleResult.Continue
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