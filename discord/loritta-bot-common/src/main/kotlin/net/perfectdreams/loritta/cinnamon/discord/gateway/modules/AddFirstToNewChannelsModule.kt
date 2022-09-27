package net.perfectdreams.loritta.cinnamon.discord.gateway.modules

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.ChannelCreate
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.DiscordGatewayEventsProcessorMetrics

class AddFirstToNewChannelsModule(private val m: LorittaBot) : ProcessDiscordEventsModule() {
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

    override suspend fun processEvent(context: GatewayEventContext): ModuleResult {
        when (context.event) {
            // ===[ CHANNEL CREATE ]===
            is ChannelCreate -> {
                // This should only be sent in a guild text channel
                if (context.event.channel.type == ChannelType.GuildText) {
                    handleFirst(
                        context.event.channel.guildId.value ?: return ModuleResult.Continue, // Pretty sure that this cannot be null here
                        context.event.channel.id
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
        val serverConfig = m.pudding.serverConfigs.getServerConfigRoot(guildId.value) ?: return
        val miscellaneousConfig = serverConfig.getMiscellaneousConfig() ?: return

        if (!miscellaneousConfig.enableQuirky)
            return

        DiscordGatewayEventsProcessorMetrics.firstTriggered
            .labels(guildId.toString())
            .inc()

        m.rest.channel.createMessage(channelId) {
            content = "First! ${FUNNY_FIRST_EMOJIS.random()}"
        }
    }
}