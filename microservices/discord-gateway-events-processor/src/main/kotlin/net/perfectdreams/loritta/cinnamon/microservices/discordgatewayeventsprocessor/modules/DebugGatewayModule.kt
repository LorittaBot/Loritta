package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.MessageCreate
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.HostnameUtils
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.GatewayProxyEventContext

class DebugGatewayModule(private val m: DiscordGatewayEventsProcessor) : ProcessDiscordEventsModule() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processEvent(context: GatewayProxyEventContext): ModuleResult {
        when (val event = context.event) {
            // ===[ CHANNEL CREATE ]===
            is MessageCreate -> {
                handleDebugGateway(context, event)
            }
            else -> {}
        }
        return ModuleResult.Continue
    }

    private suspend fun handleDebugGateway(context: GatewayProxyEventContext, messageCreate: MessageCreate) {
        // Only MrPowerGamerBR heh
        if (messageCreate.message.author.id != Snowflake(123170274651668480))
            return

        val handleStart = Clock.System.now()

        val guildId = messageCreate.message.guildId.value ?: return
        val contentInLowerCase = messageCreate.message.content.lowercase()

        val isMessage = contentInLowerCase == "<@${m.config.discord.applicationId}> debug"
        if (!isMessage)
            return

        val canTalk = m.cache.getLazyCachedLorittaPermissions(guildId, messageCreate.message.channelId).canTalk()
        if (!canTalk)
            return

        logger.info { "Received debug message made by ${messageCreate.message.author.id} in ${messageCreate.message.channelId} on ${messageCreate.message.guildId}" }

        m.rest.channel.createMessage(
            messageCreate.message.channelId
        ) {
            content = buildString {
                append("**Debug Gateway Event** (Shard: ${context.shardId} / `${HostnameUtils.getHostname()}`)")
                append("\n")
                append("**Message Timestamp:** <t:${messageCreate.message.id.timestamp.epochSeconds}:F> (<t:${messageCreate.message.id.timestamp.epochSeconds}:R>)")
                append("\n")
                append("**When the event was received by the Proxy?** <t:${context.receivedAt.epochSeconds}:F> (<t:${context.receivedAt.epochSeconds}:R>)")
                append("\n")
                append("**Difference (Received At X Message ID):** ${context.receivedAt - messageCreate.message.id.timestamp}")
                append("\n")
                append("**When the event was started being processed:** <t:${handleStart.epochSeconds}:F> (<t:${handleStart.epochSeconds}:R>)")
                append("\n")
                append("**Difference (Handle Start X Received At):** ${handleStart - context.receivedAt}")
                append("\n")
                append("**Durations:**")
                append("\n")
                for ((clazz, duration) in context.durations) {
                    append("${Emotes.SmallBlueDiamond}**${clazz.simpleName}:** $duration")
                    append("\n")
                }
            }
        }
    }
}