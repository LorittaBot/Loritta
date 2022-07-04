package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Event
import dev.kord.gateway.MessageCreate
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor

class DebugGatewayModule(private val m: DiscordGatewayEventsProcessor) : ProcessDiscordEventsModule() {
    override suspend fun processEvent(event: Event) {
        when (event) {
            // ===[ CHANNEL CREATE ]===
            is MessageCreate -> {
                handleDebugGateway(event)
            }
            else -> {}
        }
    }

    suspend fun handleDebugGateway(
        messageCreate: MessageCreate
    ) {
        val guildId = messageCreate.message.guildId.value ?: return
        val contentInLowerCase = messageCreate.message.content.lowercase()

        val isMessage = contentInLowerCase == "<@${m.config.discord.applicationId}> owo" || contentInLowerCase == "<@!${m.config.discord.applicationId}> owo"
        if (!isMessage)
            return

        val canTalk = Permission.SendMessages in m.getPermissions(guildId, messageCreate.message.channelId, Snowflake(m.config.discord.applicationId))
        if (!canTalk)
            return

        m.rest.channel.createMessage(
            messageCreate.message.channelId
        ) {
            content = "UwU! ${Emotes.LoriLick}"
        }
    }
}