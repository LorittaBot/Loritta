package net.perfectdreams.loritta.morenitta.interactions.components

import dev.minn.jda.ktx.interactions.components.replyModal
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.await

/**
 * Context of the executed command
 */
class ComponentContext(
    loritta: LorittaBot,
    config: ServerConfig,
    lorittaUser: LorittaUser,
    locale: BaseLocale,
    i18nContext: I18nContext,
    override val event: ComponentInteraction
) : InteractionContext(loritta, config, lorittaUser, locale, i18nContext) {
    suspend fun deferEdit(): InteractionHook = event.deferEdit().await()

    suspend fun sendModal(
        title: String,
        components: List<LayoutComponent>,
        callback: suspend (ModalContext, ModalArguments) -> (Unit)
    ) {
        loritta.interactivityManager.modalCallback = callback

        event.replyModal(
            "owo",
            title,
            components
        ).await()
    }
}