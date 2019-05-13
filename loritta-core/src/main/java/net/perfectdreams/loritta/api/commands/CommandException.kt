package net.perfectdreams.loritta.api.commands

class CommandException(val reason: String, val prefix: String) : RuntimeException()