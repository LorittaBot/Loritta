package net.perfectdreams.loritta.cinnamon.discord.interactions.modals

import dev.kord.core.entity.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext

open class ModalContext(
    loritta: LorittaCinnamon,
    i18nContext: I18nContext,
    user: User,
    val interaKTionsModalContext: net.perfectdreams.discordinteraktions.common.modals.ModalContext
) : InteractionContext(loritta, i18nContext, user, interaKTionsModalContext) {
    val data: String
        get() = interaKTionsModalContext.data
    val dataOrNull: String?
        get() = interaKTionsModalContext.dataOrNull
}