package net.perfectdreams.loritta.morenitta.interactions.components

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext
import net.perfectdreams.loritta.morenitta.utils.LorittaUser

/**
 * Context of the executed command
 */
class ComponentContext(
    loritta: LorittaBot,
    config: ServerConfig,
    lorittaUser: LorittaUser,
    locale: BaseLocale,
    i18nContext: I18nContext,
    override val event: IReplyCallback
) : InteractionContext(loritta, config, lorittaUser, locale, i18nContext)