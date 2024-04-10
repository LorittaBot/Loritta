package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedMentions
import net.perfectdreams.loritta.morenitta.utils.LorittaUser

/**
 * Context of the executed command
 */
class ApplicationCommandContext(
    loritta: LorittaBot,
    config: ServerConfig,
    lorittaUser: LorittaUser,
    locale: BaseLocale,
    i18nContext: I18nContext,
    val event: GenericCommandInteractionEvent
) : InteractionContext(
    loritta,
    config,
    lorittaUser,
    locale,
    i18nContext,
    UnleashedMentions(
        event.options.flatMap { it.mentions.users },
        event.options.flatMap { it.mentions.channels },
        event.options.flatMap { it.mentions.customEmojis },
        event.options.flatMap { it.mentions.roles }
    ),
    event
)