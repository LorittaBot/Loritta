package net.perfectdreams.loritta.cinnamon.discord.interactions.modals

import dev.kord.core.entity.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.common.locale.BaseLocale

open class ModalContext(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    user: User,
    val interaKTionsModalContext: net.perfectdreams.discordinteraktions.common.modals.ModalContext
) : InteractionContext(loritta, i18nContext, locale, user, interaKTionsModalContext) {
    val data: String
        get() = interaKTionsModalContext.data
    val dataOrNull: String?
        get() = interaKTionsModalContext.dataOrNull
}