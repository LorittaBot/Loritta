package net.perfectdreams.loritta.interactions.internal.commands

import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.interactions.LorittaInteractions
import net.perfectdreams.loritta.utils.locale.BaseLocale

class InteractionsCommandContext(
    val slashContext: SlashCommandContext,
    loritta: LorittaInteractions,
    command: LorittaCommand<CommandContext>,
    optionsManager: InteractionsOptionsManager,
    message: Message,
    locale: BaseLocale
) : CommandContext(loritta, command, optionsManager, message, locale)