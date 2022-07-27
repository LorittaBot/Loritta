package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.common.commands.CommandRegistry
import net.perfectdreams.discordinteraktions.common.commands.options.NameableCommandOption
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon

class CommandRegistry(
    val loritta: LorittaCinnamon,
    val interaKTionsManager: CommandManager,
    val interaKTionsRegistry: CommandRegistry
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun register(declaration: Any, vararg executors: Any) {}

    suspend fun updateAllCommands() {
        if (loritta.interactionsConfig.registerGlobally) {
            interaKTionsRegistry.updateAllGlobalCommands()
        } else {
            for (guildId in loritta.interactionsConfig.guildsToBeRegistered) {
                interaKTionsRegistry.updateAllCommandsInGuild(Snowflake(guildId))
            }
        }

        logger.info { "Command Character Usage:" }
        interaKTionsManager.applicationCommandsDeclarations
            .filterIsInstance<net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclaration>()
            .forEach {
                var sum = 0

                sum += it.name.length
                sum += it.description.length

                sum += it.executor?.options?.registeredOptions?.filterIsInstance<NameableCommandOption<*>>()?.sumOf {
                    it.name.length + it.description.length
                } ?: 0

                it.subcommands.forEach {
                    sum += it.name.length
                    sum += it.description.length
                    sum += it.executor?.options?.registeredOptions?.filterIsInstance<NameableCommandOption<*>>()?.sumOf {
                        it.name.length + it.description.length
                    } ?: 0
                }

                it.subcommandGroups.forEach {
                    sum += it.name.length
                    sum += it.description.length

                    it.subcommands.forEach {
                        sum += it.name.length
                        sum += it.description.length
                        sum += it.executor?.options?.registeredOptions?.filterIsInstance<NameableCommandOption<*>>()?.sumOf {
                            it.name.length + it.description.length
                        } ?: 0
                    }
                }

                logger.info { "${it.name}: $sum/4000" }
            }
    }
}