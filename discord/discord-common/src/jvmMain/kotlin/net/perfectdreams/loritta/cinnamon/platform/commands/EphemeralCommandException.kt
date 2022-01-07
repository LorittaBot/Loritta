package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.discordinteraktions.common.builder.message.create.InteractionOrFollowupMessageCreateBuilder

/**
 * Useful for command control flow, this allows you a quick and easy way to "halt" the execution of an command.
 *
 * Instead of showing the pre-defined generic error message, the [builder] message should be sent to the user.
 *
 * Implementations should catch this exception and send the [builder], logging the error is not required.
 */
class EphemeralCommandException(val builder: InteractionOrFollowupMessageCreateBuilder.() -> (Unit)) : RuntimeException()