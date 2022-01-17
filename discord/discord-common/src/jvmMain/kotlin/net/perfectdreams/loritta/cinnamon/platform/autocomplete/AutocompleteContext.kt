package net.perfectdreams.loritta.cinnamon.platform.autocomplete

import net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteContext
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon

// This doesn't inherit from InteractionContext because we can't send messages on a autocomplete request
open class AutocompleteContext(
    val loritta: LorittaCinnamon,
    val i18nContext: I18nContext,
    val sender: User,
    val interaKTionsContext: AutocompleteContext
)