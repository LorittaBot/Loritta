package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Event
import dev.kord.gateway.MessageCreate
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.BomDiaECia

class BomDiaECiaModule(private val m: DiscordGatewayEventsProcessor) : ProcessDiscordEventsModule() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processEvent(event: Event) {
        when (event) {
            // ===[ CHANNEL CREATE ]===
            is MessageCreate -> {
                val guildId = event.message.guildId.value
                val channelId = event.message.channelId

                if (guildId != null) {
                    handleMessage(
                        guildId,
                        channelId,
                        event.message.author.id
                    )
                }
            }
            else -> {}
        }
    }

    private suspend fun handleMessage(
        guildId: Snowflake,
        channelId: Snowflake,
        userId: Snowflake
    ) {
        if (channelId != Snowflake(297732013006389252L))
            return

        val serverConfig = m.services.serverConfigs.getServerConfigRoot(guildId.value) ?: return
        val miscellaneousConfig = serverConfig.getMiscellaneousConfig() ?: return

        if (!miscellaneousConfig.enableBomDiaECia)
            return

        logger.info { "Triggered!" }
        // Update cached info to store the popular channels
        m.bomDiaECia.activeTextChannels.getOrPut(channelId) { BomDiaECia.YudiTextChannelInfo(guildId) }.apply {
            lastMessageSent = System.currentTimeMillis()
            users.add(userId)
        }
    }
}