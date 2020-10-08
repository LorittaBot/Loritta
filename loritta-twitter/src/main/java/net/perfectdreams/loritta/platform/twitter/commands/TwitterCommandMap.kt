package net.perfectdreams.loritta.platform.twitter.commands

import mu.KotlinLogging
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.CommandMap

class TwitterCommandMap : CommandMap<Command<CommandContext>> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val commands = mutableListOf<Command<CommandContext>>()

    override fun register(command: Command<CommandContext>) {
        logger.info { "Registering $command with ${command.labels}" }
        commands.add(command)
    }

    override fun unregister(command: Command<CommandContext>) {
        logger.info { "Unregistering $command..." }
        commands.remove(command)
    }
}