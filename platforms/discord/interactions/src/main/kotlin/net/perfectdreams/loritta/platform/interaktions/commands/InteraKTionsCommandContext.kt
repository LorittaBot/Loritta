package net.perfectdreams.loritta.platform.interaktions.commands

import net.perfectdreams.discordinteraktions.common.context.commands.ApplicationCommandContext
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.discord.objects.LorittaGuild
import net.perfectdreams.loritta.platform.interaktions.LorittaInteraKTions
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsInteractionMessageChannel

class InteraKTionsCommandContext(
    loritta: LorittaInteraKTions,
    i18nContext: I18nContext,
    user: User,
    channel: InteraKTionsInteractionMessageChannel,
    guild: LorittaGuild?,
    val slashCommandContext: ApplicationCommandContext
) : DiscordCommandContext(loritta, i18nContext, user, channel, guild)