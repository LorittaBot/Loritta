package net.perfectdreams.loritta.platform.interaktions.commands

import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.api.entities.Snowflake
import net.perfectdreams.loritta.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.common.commands.CommandContext

class WaifuDiscordMentionTextConverter(private val rest: RestClient) : RateWaifuExecutor.TextConverter {
    override suspend fun convert(context: CommandContext, input: String): String {
        // This should never happen because it should always be a InteraKTions Context!
        // This should also be removed if we add a concept of "resolved objects" to Loritta messages
        if (context !is InteraKTionsCommandContext)
            throw UnsupportedOperationException("Unsupported Context Type! $context")

        if (input.startsWith("<@") && input.endsWith(">")) {
            // Is a mention... maybe?
            val userId = input.removePrefix("<@")
                .removePrefix("!")
                .removeSuffix(">")
                .toLongOrNull() ?: return input // If the input is not a long, then return the input

            val user = context.slashCommandContext.data.resolved?.users?.get(Snowflake(userId)) ?: return input // If there isn't any matching user, then return the input
            return user.name
        }

        return input
    }
}