package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.discordinteraktions.common.context.commands.ApplicationCommandContext
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.platform.InteractionContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon

class ApplicationCommandContext(
    loritta: LorittaCinnamon,
    i18nContext: I18nContext,
    user: User,
    override val interaKTionsContext: ApplicationCommandContext
) : InteractionContext(loritta, i18nContext, user, interaKTionsContext)