package net.perfectdreams.loritta.cinnamon.discord.gateway.modules

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.MessageCreate
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.HostnameUtils
import net.perfectdreams.loritta.deviousfun.PermissionsWrapper
import net.perfectdreams.loritta.morenitta.LorittaBot

class DebugGatewayModule(private val m: LorittaBot) : ProcessDiscordEventsModule() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processEvent(context: GatewayEventContext): ModuleResult {
        when (val event = context.event) {
            // ===[ CHANNEL CREATE ]===
            is MessageCreate -> {
                handleDebugGateway(context, event)
            }

            else -> {}
        }
        return ModuleResult.Continue
    }

    private suspend fun handleDebugGateway(context: GatewayEventContext, messageCreate: MessageCreate) {
        // Only MrPowerGamerBR heh
        if (messageCreate.message.author.id != Snowflake(123170274651668480))
            return

        val handleStart = Clock.System.now()

        val guildId = messageCreate.message.guildId.value ?: return
        val contentInLowerCase = messageCreate.message.content.lowercase()

        val isMessage = contentInLowerCase == "<@${m.config.loritta.discord.applicationId}> debug"
        if (!isMessage)
            return

        val canTalk = PermissionsWrapper(m.cache.getLorittaPermissions(guildId, messageCreate.message.channelId).permissions).canTalk()
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