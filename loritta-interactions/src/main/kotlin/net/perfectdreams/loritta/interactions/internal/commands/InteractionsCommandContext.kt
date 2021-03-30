package net.perfectdreams.loritta.interactions.internal.commands

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.interactions.LorittaInteractions
import net.perfectdreams.loritta.utils.locale.BaseLocale

class InteractionsCommandContext(
    loritta: LorittaInteractions,
    command: LorittaCommand<CommandContext>,
    message: Message,
    locale: BaseLocale
) : CommandContext(loritta, command, message, locale) {
    override suspend fun user(argument: Int): User? {
        TODO("Not yet implemented")
    }

    override suspend fun imageUrl(argument: Int, searchPreviousMessages: Int): String? {
        TODO("Not yet implemented")
    }

    override suspend fun image(
        argument: Int,
        searchPreviousMessages: Int,
        createTextAsImageIfNotFound: Boolean
    ): Image? {
        TODO("Not yet implemented")
    }

    override suspend fun explain() {
        TODO("Not yet implemented")
    }

}