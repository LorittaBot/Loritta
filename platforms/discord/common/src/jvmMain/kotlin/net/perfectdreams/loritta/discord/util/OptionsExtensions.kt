package net.perfectdreams.loritta.discord.util

import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.discord.objects.LorittaDiscordChannel

fun CommandOptions.channel(name: String, description: LocaleKeyData) = argument<LorittaDiscordChannel>(
    CommandOptionType.Channel,
    name,
    description
)

fun CommandOptions.optionalChannel(name: String, description: LocaleKeyData) = argument<LorittaDiscordChannel?>(
    CommandOptionType.NullableChannel,
    name,
    description
)