package net.perfectdreams.loritta.common.commands

/**
 * Useful for command control flow, this allows you a quick and easy way to "halt" the execution of an command.
 *
 * Implementations should catch this exception and just halt the command execution, logging the error is not required.
 */
class SilentCommandException : RuntimeException()