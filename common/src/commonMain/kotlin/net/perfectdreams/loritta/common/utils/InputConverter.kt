package net.perfectdreams.loritta.common.utils

import net.perfectdreams.loritta.common.commands.CommandContext

/**
 * Interface used to convert an input to an output
 *
 * This is used within commands to convert platform-specific mentions into plain text
 */
interface InputConverter<InputType, OutputType> {
    suspend fun convert(context: CommandContext, input: InputType): OutputType

    /**
     * An noop convert operation, always returns the input
     */
    class NoopTextConverter : InputConverter<String, String> {
        override suspend fun convert(context: CommandContext, input: String) = input
    }
}