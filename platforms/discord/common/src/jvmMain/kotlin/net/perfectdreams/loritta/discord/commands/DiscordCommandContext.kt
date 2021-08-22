package net.perfectdreams.loritta.discord.commands

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.entities.InteractionMessageChannel
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.discord.LorittaDiscord
import net.perfectdreams.loritta.discord.objects.LorittaGuild

abstract class DiscordCommandContext(
    override val loritta: LorittaDiscord,
    i18nContext: I18nContext,
    user: User,
    override val channel: InteractionMessageChannel,
    val guild: LorittaGuild?
) : CommandContext(loritta, i18nContext, user, channel)