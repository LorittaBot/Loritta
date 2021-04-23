package net.perfectdreams.loritta.discord.command

import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.discord.LorittaDiscord
import net.perfectdreams.loritta.discord.objects.LorittaDiscordMessageChannel
import net.perfectdreams.loritta.discord.objects.LorittaGuild

abstract class DiscordCommandContext(
    loritta: LorittaDiscord,
    locale: BaseLocale,
    user: User,
    override val channel: LorittaDiscordMessageChannel,
    val guild: LorittaGuild?
) : CommandContext(loritta, locale, user, channel)