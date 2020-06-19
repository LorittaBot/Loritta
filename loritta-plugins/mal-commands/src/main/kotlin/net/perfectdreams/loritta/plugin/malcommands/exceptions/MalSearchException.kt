package net.perfectdreams.loritta.plugin.malcommands.exceptions

class MalSearchException(override val cause: Throwable? = null):
        MalException("Failed at searching/querying anime")