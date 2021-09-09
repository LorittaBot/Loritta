package net.perfectdreams.loritta.cinnamon.platform.interaktions.commands

import net.perfectdreams.discordinteraktions.common.context.commands.ApplicationCommandContext
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.entities.User
import net.perfectdreams.loritta.cinnamon.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.cinnamon.discord.objects.LorittaGuild
import net.perfectdreams.loritta.cinnamon.platform.interaktions.LorittaInteraKTions
import net.perfectdreams.loritta.cinnamon.platform.interaktions.entities.InteraKTionsInteractionMessageChannel

class InteraKTionsCommandContext(
    loritta: LorittaInteraKTions,
    i18nContext: I18nContext,
    user: User,
    channel: InteraKTionsInteractionMessageChannel,
    guild: LorittaGuild?,
    val slashCommandContext: ApplicationCommandContext
) : DiscordCommandContext(loritta, i18nContext, user, channel, guild)