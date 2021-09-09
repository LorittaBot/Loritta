package net.perfectdreams.loritta.cinnamon.platform.kord.commands

import net.perfectdreams.loritta.cinnamon.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.locale.BaseLocale
import net.perfectdreams.loritta.cinnamon.discord.command.DiscordCommandContext
import net.perfectdreams.loritta.cinnamon.discord.objects.LorittaDiscordMessageChannel
import net.perfectdreams.loritta.cinnamon.discord.objects.LorittaGuild
import net.perfectdreams.loritta.cinnamon.platform.kord.LorittaKord

class KordCommandContext(
    override val loritta: LorittaKord,
    locale: BaseLocale,
    user: User,
    channel: LorittaDiscordMessageChannel,
    guild: LorittaGuild?
) : DiscordCommandContext(loritta, locale, user, channel, guild)