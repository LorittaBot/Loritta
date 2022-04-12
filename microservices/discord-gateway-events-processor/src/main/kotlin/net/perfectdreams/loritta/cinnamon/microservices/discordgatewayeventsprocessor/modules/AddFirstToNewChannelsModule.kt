package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor

class AddFirstToNewChannelsModule(private val m: DiscordGatewayEventsProcessor) {
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