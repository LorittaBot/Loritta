package net.perfectdreams.loritta.cinnamon.discord.interactions

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.serializer
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.options.NameableCommandOption
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.pudding.tables.DiscordLorittaApplicationCommandHashes
import net.perfectdreams.loritta.cinnamon.pudding.utils.HashEncoder
import java.util.*

class InteractionsRegistry(
    val loritta: LorittaCinnamon,
    val manager: InteractionsManager
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun updateAllCommands() {
        loritta.services.hikariDataSource.connection.use { connection ->
            // First, we will hold a lock to avoid other instances trying to update the app commands at the same time
            val xactLockStatement = connection.prepareStatement("SELECT pg_advisory_xact_lock(?);")
            xactLockStatement.setInt(1, "loritta-cinnamon-application-command-updater".hashCode())
            xactLockStatement.execute()

            if (loritta.config.interactions.registerGlobally) {
                val storedHash = connection.prepareStatement("SELECT hash FROM ${DiscordLorittaApplicationCommandHashes.tableName} WHERE id = 0;")
                    .executeQuery()
                    .let {
                        if (it.next())
                            it.getInt("hash")
                        else
                            null
                    }

                val currentHash = loritta.cache.hashEntity(manager.interaKTions.createGlobalApplicationCommandCreateRequests())

                if (currentHash != storedHash) {
                    // Needs to be updated!
                    logger.info { "Updating Loritta global commands... Hash: $currentHash" }
                    manager.interaKTions.updateAllGlobalCommands()

                    val updateStatement = connection.prepareStatement("INSERT INTO ${DiscordLorittaApplicationCommandHashes.tableName} (id, hash) VALUES (0, $currentHash) ON CONFLICT (id) DO UPDATE SET hash = $currentHash;")
                    updateStatement.executeUpdate()

                    logger.info { "Successfully updated Loritta's global commands! Hash: $currentHash" }
                } else {
                    // No need for update, yay :3
                    logger.info { "Stored global commands hash match our hash $currentHash, so we don't need to update, yay! :3" }
                }
            } else {
                for (guildId in loritta.config.interactions.guildsToBeRegistered) {
                    val storedHash = connection.prepareStatement("SELECT hash FROM ${DiscordLorittaApplicationCommandHashes.tableName} WHERE id = $guildId;")
                        .executeQuery()
                        .let {
                            if (it.next())
                                it.getInt("hash")
                            else
                                null
                        }

                    val currentHash = loritta.cache.hashEntity(manager.interaKTions.createGlobalApplicationCommandCreateRequests())

                    if (currentHash != storedHash) {
                        // Needs to be updated!
                        logger.info { "Updating Loritta guild commands on $guildId... Hash: $currentHash" }
                        manager.interaKTions.updateAllCommandsInGuild(Snowflake(guildId))

                        val updateStatement = connection.prepareStatement("INSERT INTO ${DiscordLorittaApplicationCommandHashes.tableName} (id, hash) VALUES ($guildId, $currentHash) ON CONFLICT (id) DO UPDATE SET hash = $currentHash;")
                        updateStatement.executeUpdate()

                        logger.info { "Successfully updated Loritta's guild commands on $guildId! Hash: $currentHash" }
                    } else {
                        // No need for update, yay :3
                        logger.info { "Stored guild commands for guild $guildId hash match our hash $currentHash, so we don't need to update, yay! :3" }
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
    }

    /**
     * Hashes [value]'s primitives with [Objects.hash] to create a hash that identifies the object.
     */
    // TODO: This is the same implementation used in the DiscordCacheService... maybe
    inline fun <reified T> hashEntity(value: T): Int {
        // We use our own custom hash encoder because ProtoBuf can't encode the "Optional" fields, because it can't serialize null values
        // on a field that isn't marked as null
        val encoder = HashEncoder()
        encoder.encodeSerializableValue(serializer(), value)
        return Objects.hash(*encoder.list.toTypedArray())
    }
}