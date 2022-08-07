package net.perfectdreams.loritta.cinnamon.discord.interactions

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.options.NameableCommandOption
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon

class InteractionsRegistry(
    val loritta: LorittaCinnamon,
    val manager: InteractionsManager
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun updateAllCommands() {
        if (loritta.config.interactions.registerGlobally) {
            manager.interaKTions.updateAllGlobalCommands()
        } else {
            for (guildId in loritta.config.interactions.guildsToBeRegistered) {
                manager.interaKTions.updateAllCommandsInGuild(Snowflake(guildId))
            }
        }

        logger.info { "Command Character Usage:" }
        manager.interaKTions.manager.applicationCommandsDeclarations
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