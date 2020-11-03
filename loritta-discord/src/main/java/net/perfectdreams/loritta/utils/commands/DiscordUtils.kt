package net.perfectdreams.loritta.utils.commands

import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext

fun DiscordCommandContext.getTextChannel(input: String?, executedIfNull: Boolean = false): TextChannel? = runCatching {
    if (input == null)
        return discordMessage.textChannel

    val channels = guild.getTextChannelsByName(input, false)
    if (channels.isNotEmpty()) {
        return channels[0]
    }

    val id = input
            .replace("<", "")
            .replace("#", "")
            .replace(">", "")

    if (!id.isValidSnowflake())
        return null

    val channel = guild.getTextChannelById(id)

    return@runCatching if (channel == null && discordMessage.channel is TextChannel && executedIfNull) discordMessage.textChannel else channel
}.getOrNull()