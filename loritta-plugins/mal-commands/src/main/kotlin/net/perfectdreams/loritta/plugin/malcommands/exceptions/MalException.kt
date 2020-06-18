package net.perfectdreams.loritta.plugin.malcommands.exceptions

open class MalException(override val message: String? = null, override val cause: Throwable? = null)
    : Exception()
