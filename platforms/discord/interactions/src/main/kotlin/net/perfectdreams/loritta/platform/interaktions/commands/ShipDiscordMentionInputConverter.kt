package net.perfectdreams.loritta.platform.interaktions.commands

import net.perfectdreams.discordinteraktions.api.entities.Snowflake
import net.perfectdreams.loritta.commands.`fun`.ShipExecutor
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.utils.InputConverter
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsUser

class ShipDiscordMentionInputConverter : InputConverter<String, ShipExecutor.ConverterResult> {
    // From JDA
    private val userRegex = Regex("<@!?(\\d+)>")
    private val emoteRegex = Regex("<(a)?:([a-zA-Z0-9_]+):([0-9]+)>")

    override suspend fun convert(context: CommandContext, input: String): ShipExecutor.ConverterResult {
        // This should never happen because it should always be a InteraKTions Context!
        // This should also be removed if we add a concept of "resolved objects" to Loritta messages
        if (context !is InteraKTionsCommandContext)
            throw UnsupportedOperationException("Unsupported Context Type! $context")

        // Check for user mention
        val userMatch = userRegex.matchEntire(input)
        if (userMatch != null) {
            // Is a mention... maybe?
            val userId = userMatch.groupValues[1].toLongOrNull() ?: return ShipExecutor.StringResult(input) // If the input is not a long, then return the input
            val user = context.slashCommandContext.data.resolved?.users?.get(Snowflake(userId)) ?: return ShipExecutor.StringResult(input) // If there isn't any matching user, then return the input
            return ShipExecutor.UserResult(InteraKTionsUser(user))
        }

        // Check for emote mention
        val emoteMatch = emoteRegex.matchEntire(input)
        if (emoteMatch != null) {
            val isAnimated = emoteMatch.groupValues[1].isNotEmpty()
            val extension = if (isAnimated) "gif" else "png"

            return ShipExecutor.StringWithImageResult(
                emoteMatch.groupValues[2],
                "https://cdn.discordapp.com/emojis/${emoteMatch.groupValues[3]}.$extension?v=1"
            )
        }

        return ShipExecutor.StringResult(input)
    }
}