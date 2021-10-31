package net.perfectdreams.loritta.common.commands

import net.perfectdreams.loritta.common.builder.MessageBuilder
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.LorittaReply

/**
 * Useful for command control flow, this allows you a quick and easy way to "halt" the execution of an command.
 *
 * Instead of showing the pre-defined generic error message, the [lorittaMessage] should be sent to the user.
 *
 * Implementations should catch this exception and send the [lorittaMessage], logging the error is not required.
 */
class CommandException(val lorittaMessage: LorittaMessage) : RuntimeException()