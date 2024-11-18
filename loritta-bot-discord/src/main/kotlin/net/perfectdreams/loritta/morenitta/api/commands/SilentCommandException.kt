package net.perfectdreams.loritta.morenitta.api.commands

/**
 * Thrown to indicate that the command flow should be halted, however no output should be sent to the user
 * or to the console.
 *
 * Used when the code already explained to the user the issue and the flow should be halted.
 */
class SilentCommandException : RuntimeException(null, null, false, false)