package net.perfectdreams.loritta.platform.interaktions.commands

import net.perfectdreams.discordinteraktions.common.context.commands.SlashCommandContext
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.discord.command.DiscordCommandContext
import net.perfectdreams.loritta.discord.objects.LorittaDiscordMessageChannel
import net.perfectdreams.loritta.discord.objects.LorittaGuild
import net.perfectdreams.loritta.platform.interaktions.LorittaInteraKTions

class InteraKTionsCommandContext(
    loritta: LorittaInteraKTions,
    locale: BaseLocale,
    user: User,
    channel: LorittaDiscordMessageChannel,
    guild: LorittaGuild?,
    val slashCommandContext: SlashCommandContext
) : DiscordCommandContext(loritta, locale, user, channel, guild)