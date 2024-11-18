package net.perfectdreams.loritta.morenitta.api.commands

import net.perfectdreams.loritta.morenitta.messages.LorittaReply

/**
 * Thrown to indicate that the command flow should be halted due to issues.
 *
 * The [reply] construtor parameter is used to notify the user about the issue.
 */
class CommandException(val reply: LorittaReply) : RuntimeException(null, null, false, false) {
    constructor(reason: String, prefix: String) : this(LorittaReply(reason, prefix))
}