package net.perfectdreams.loritta.platform.kord.commands

import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.discord.command.DiscordCommandContext
import net.perfectdreams.loritta.discord.objects.LorittaDiscordMessageChannel
import net.perfectdreams.loritta.discord.objects.LorittaGuild
import net.perfectdreams.loritta.platform.kord.LorittaKord

class KordCommandContext(
    override val loritta: LorittaKord,
    locale: BaseLocale,
    user: User,
    channel: LorittaDiscordMessageChannel,
    guild: LorittaGuild?
) : DiscordCommandContext(loritta, locale, user, channel, guild)