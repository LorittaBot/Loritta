package net.perfectdreams.loritta.api.commands

import net.perfectdreams.loritta.api.messages.LorittaReply

/**
 * Thrown to indicate that the command flow should be halted due to issues.
 *
 * The [reply] construtor parameter is used to notify the user about the issue.
 */
class CommandException(val reply: LorittaReply) : RuntimeException() {
    constructor(reason: String, prefix: String) : this(LorittaReply(reason, prefix))
}