package net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.LorittaUser

class AutocompleteContext(
    val loritta: LorittaBot,
    val config: ServerConfig,
    var lorittaUser: LorittaUser,
    val locale: BaseLocale,
    val i18nContext: I18nContext,
    val event: CommandAutoCompleteInteractionEvent
)