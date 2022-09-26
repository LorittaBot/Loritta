package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandContext
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon

open class ApplicationCommandContext(
    loritta: LorittaCinnamon,
    i18nContext: I18nContext,
    user: User,
    override val interaKTionsContext: ApplicationCommandContext
) : InteractionContext(loritta, i18nContext, user, interaKTionsContext)