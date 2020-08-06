package net.perfectdreams.loritta.api.commands

/**
 * Thrown to indicate that the command flow should be halted due to issues.
 *
 * The [reason] and [prefix] construtor parameters are used to notify the user about the issue.
 */
class CommandException(val reason: String, val prefix: String) : RuntimeException()