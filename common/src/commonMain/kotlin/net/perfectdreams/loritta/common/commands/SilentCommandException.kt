package net.perfectdreams.loritta.common.commands

import net.perfectdreams.loritta.common.builder.MessageBuilder
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.LorittaReply

/**
 * Useful for command control flow, this allows you a quick and easy way to "halt" the execution of an command.
 *
 * Implementations should catch this exception and just halt the command execution, logging the error is not required.
 */
class SilentCommandException : RuntimeException()