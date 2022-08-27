package net.perfectdreams.loritta.cinnamon.discord.interactions

import dev.kord.common.entity.DiscordApplicationCommand
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.options.NameableCommandOption
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.CommandMentions
import net.perfectdreams.loritta.cinnamon.pudding.tables.DiscordLorittaApplicationCommandHashes
import net.perfectdreams.loritta.cinnamon.pudding.utils.HashEncoder
import org.postgresql.util.PGobject
import java.util.*

class InteractionsRegistry(
    val loritta: LorittaCinnamon,
    val manager: InteractionsManager
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun updateAllCommands() {
        var registeredCommands: List<DiscordApplicationCommand>? = null

        loritta.services.hikariDataSource.connection.use { connection ->
            // First, we will hold a lock to avoid other instances trying to update the app commands at the same time
            val xactLockStatement = connection.prepareStatement("SELECT pg_advisory_xact_lock(?);")
            xactLockStatement.setInt(1, "loritta-cinnamon-application-command-updater".hashCode())
            xactLockStatement.execute()

            if (loritta.config.interactions.registerGlobally) {
                val pairData = connection.prepareStatement("SELECT hash, data FROM ${DiscordLorittaApplicationCommandHashes.tableName} WHERE id = 0;")
                    .executeQuery()
                    .let {
                        if (it.next())
                            Pair(it.getInt("hash"), it.getString("data"))
                        else
                            null
                    }

                val currentHash = loritta.cache.hashEntity(manager.interaKTions.createGlobalApplicationCommandCreateRequests())

                if (pairData == null || currentHash != pairData.first) {
                    // Needs to be updated!
                    logger.info { "Updating Loritta global commands... Hash: $currentHash" }
                    val updatedCommands = manager.interaKTions.updateAllGlobalCommands()

                    val updateStatement = connection.prepareStatement("INSERT INTO ${DiscordLorittaApplicationCommandHashes.tableName} (id, hash, data) VALUES (0, $currentHash, ?) ON CONFLICT (id) DO UPDATE SET hash = $currentHash, data = ?;")

                    val pgObject = PGobject()
                    pgObject.type = "jsonb"
                    pgObject.value = Json.encodeToString(updatedCommands)
                    updateStatement.setObject(1, pgObject)
                    updateStatement.setObject(2, pgObject)
                    updateStatement.executeUpdate()

                    logger.info { "Successfully updated Loritta's global commands! Hash: $currentHash" }
                    registeredCommands = updatedCommands
                } else {
                    // No need for update, yay :3
                    logger.info { "Stored global commands hash match our hash $currentHash, so we don't need to update, yay! :3" }
                    registeredCommands = Json.decodeFromString(pairData.second!!)
                }
            } else {
                for (guildId in loritta.config.interactions.guildsToBeRegistered) {
                    val pairData = connection.prepareStatement("SELECT hash, data FROM ${DiscordLorittaApplicationCommandHashes.tableName} WHERE id = $guildId;")
                        .executeQuery()
                        .let {
                            if (it.next())
                                Pair(it.getInt("hash"), it.getString("data"))
                            else
                                null
                        }

                    val currentHash = loritta.cache.hashEntity(manager.interaKTions.createGlobalApplicationCommandCreateRequests())

                    if (pairData == null || currentHash != pairData.first) {
                        // Needs to be updated!
                        logger.info { "Updating Loritta guild commands on $guildId... Hash: $currentHash" }
                        val updatedCommands = manager.interaKTions.updateAllCommandsInGuild(Snowflake(guildId))

                        val updateStatement = connection.prepareStatement("INSERT INTO ${DiscordLorittaApplicationCommandHashes.tableName} (id, hash, data) VALUES ($guildId, $currentHash, ?) ON CONFLICT (id) DO UPDATE SET hash = $currentHash, data = ?;")

                        val pgObject = PGobject()
                        pgObject.type = "jsonb"
                        pgObject.value = Json.encodeToString(updatedCommands)
                        updateStatement.setObject(1, pgObject)
                        updateStatement.setObject(2, pgObject)
                        updateStatement.executeUpdate()

                        logger.info { "Successfully updated Loritta's guild commands on $guildId! Hash: $currentHash" }
                        registeredCommands = updatedCommands
                    } else {
                        // No need for update, yay :3
                        logger.info { "Stored guild commands for guild $guildId hash match our hash $currentHash, so we don't need to update, yay! :3" }
                        registeredCommands = Json.decodeFromString(pairData.second!!)
                    }
                }
            }

            connection.commit()
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

        loritta.commandMentions = CommandMentions(registeredCommands ?: error("At this point, the registeredCommands should be already initialized... So if you are seeing this, then it means that something went terribly wrong!"))
    }
}