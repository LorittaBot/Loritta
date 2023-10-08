package net.perfectdreams.loritta.cinnamon.discord.gateway.modules

import dev.kord.gateway.MessageCreate
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.DiscordGatewayEventsProcessorMetrics

class OwOGatewayModule(private val m: LorittaBot) : ProcessDiscordEventsModule() {
    override suspend fun processEvent(context: GatewayEventContext): ModuleResult {
        when (val event = context.event) {
            // ===[ CHANNEL CREATE ]===
            is MessageCreate -> {
                handleOwOGateway(event)
            }
            else -> {}
        }
        return ModuleResult.Continue
    }

    private suspend fun handleOwOGateway(
        messageCreate: MessageCreate
    ) {
        val guildId = messageCreate.message.guildId.value ?: return
        val contentInLowerCase = messageCreate.message.content.lowercase()

        val isMessage = contentInLowerCase == "<@${m.config.loritta.discord.applicationId}> owo" || contentInLowerCase == "<@!${m.config.loritta.discord.applicationId}> owo"
        if (!isMessage)
            return

        val canTalk = m.cache.getLazyCachedLorittaPermissions(guildId, messageCreate.message.channelId).canTalk()
        if (!canTalk)
            return

        DiscordGatewayEventsProcessorMetrics.owoTriggered
            .labels(guildId.toString())
            .inc()

        m.rest.channel.createMessage(
            messageCreate.message.channelId
        ) {
            content = "UwU! ${Emotes.LoriLick}"
        }
    }
}