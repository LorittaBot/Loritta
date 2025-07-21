package net.perfectdreams.loritta.morenitta.interactions.modals

import net.dv8tion.jda.api.interactions.modals.ModalInteraction
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedHook
import net.perfectdreams.loritta.morenitta.interactions.UnleashedMentions
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.await

/**
 * Context of the executed command
 */
class ModalContext(
    loritta: LorittaBot,
    config: ServerConfig,
    lorittaUser: LorittaUser,
    locale: BaseLocale,
    i18nContext: I18nContext,
    val event: ModalInteraction
) : InteractionContext(loritta, config, lorittaUser, locale, i18nContext, UnleashedMentions(emptyList(), emptyList(), emptyList(), emptyList()), event) {
    suspend fun deferEdit() = UnleashedHook.InteractionHook(event.deferEdit().await())
}