package net.perfectdreams.loritta.interactions.internal.commands

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.interactions.LorittaInteractions
import net.perfectdreams.loritta.utils.locale.BaseLocale

class InteractionsCommandContext(
    loritta: LorittaInteractions,
    command: LorittaCommand<CommandContext>,
    message: Message,
    locale: BaseLocale
) : CommandContext(loritta, command, message, locale)