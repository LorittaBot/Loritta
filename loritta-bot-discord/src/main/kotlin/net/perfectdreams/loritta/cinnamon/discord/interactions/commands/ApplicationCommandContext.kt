package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandContext
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot

open class ApplicationCommandContext(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    user: User,
    override val interaKTionsContext: ApplicationCommandContext
) : InteractionContext(loritta, i18nContext, locale, user, interaKTionsContext)