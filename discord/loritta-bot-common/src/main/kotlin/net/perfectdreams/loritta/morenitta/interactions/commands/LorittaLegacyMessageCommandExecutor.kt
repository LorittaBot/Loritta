package net.perfectdreams.loritta.morenitta.interactions.commands

import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

/**
 * Provides support for legacy message commands in an interactions framework by manually mapping the arguments into Slash Commands arguments
 */
interface LorittaLegacyMessageCommandExecutor {
    companion object {
        val NO_ARGS = emptyMap<OptionReference<*>, Any>()
    }

    /**
     * Converts legacy message command arguments into InteraKTions Unleashed's arguments from the current message command context
     *
     * If the result is null, the command execution will be halted!
     */
    suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?>?
}