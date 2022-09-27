package net.perfectdreams.loritta.cinnamon.discord.gateway.modules

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.MessageCreate
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext

class BomDiaECiaModule(private val m: LorittaBot) : ProcessDiscordEventsModule() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processEvent(context: GatewayEventContext): ModuleResult {
        when (context.event) {
            // ===[ CHANNEL CREATE ]===
            is MessageCreate -> {
                val guildId = context.event.message.guildId.value
                val channelId = context.event.message.channelId

                if (guildId != null) {
                    handleMessage(
                        guildId,
                        channelId,
                        context.event.message.author.id
                    )
                }
            }
            else -> {}
        }
        return ModuleResult.Continue
    }

    private suspend fun handleMessage(
        guildId: Snowflake,
        channelId: Snowflake,
        userId: Snowflake
    ) {
        if (channelId != Snowflake(297732013006389252L))
            return

        val serverConfig = m.pudding.serverConfigs.getServerConfigRoot(guildId.value) ?: return
        val miscellaneousConfig = serverConfig.getMiscellaneousConfig() ?: return

        if (!miscellaneousConfig.enableBomDiaECia)
            return

        logger.info { "Triggered!" }
        // Update cached info to store the popular channels
        /* m.bomDiaECia.activeTextChannels.getOrPut(channelId) { BomDiaECia.YudiTextChannelInfo(guildId) }.apply {
            lastMessageSent = System.currentTimeMillis()
            users.add(userId)
        } */
    }
}