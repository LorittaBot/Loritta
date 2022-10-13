package net.perfectdreams.loritta.morenitta.platform.discord.utils

import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.MuteCommand
import net.perfectdreams.loritta.morenitta.dao.Mute
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.tables.Mutes
import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.morenitta.dao.servers.Giveaway
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.tables.servers.Giveaways
import net.perfectdreams.loritta.morenitta.utils.giveaway.GiveawayManager
import org.jetbrains.exposed.sql.and

/**
 * Prepares and setups guilds in batches
 *
 * Doing everything individually causes the database to overload because sending 10k+ transactions every time a
 * shard is up takes a LOT of processing power.
 *
 * The setup queue tries to batch guilds, reducing the 10k+ transactions to get the server config to less than 10 transactions, by
 * doing a select + in list statement.
 *
 * This also speeds up setup, because retrieving guilds in a batch is faster than doing individually.
 */
class GuildSetupQueue(val loritta: LorittaBot) {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    private val pendingGuilds = mutableMapOf<Long, Guild>()
    private var job: Job? = null
    private val mutex = Mutex()
    private val creatingJobMutex = Mutex()

    /**
     * Adds the guild to a setup queue and creates a job to setup all guilds in the setup queue after 3s.
     * (or in this instant, if there is more than 10_000 guilds in the queue)
     *
     * @param guild the guild that will be added to the setup queue
     */
    suspend fun addToSetupQueue(guild: Guild) {
        mutex.withLock {
            pendingGuilds[guild.idLong] = guild
        }

        // If the creating job mutex is locked, we don't need to create it again
        if (creatingJobMutex.isLocked)
            return

        creatingJobMutex.withLock {
            // Cancel the currently running job
            job?.cancel()

            // And run another one!
            job = GlobalScope.launch(loritta.coroutineDispatcher) {
                delay(3_000) // 3s

                // If not...
                // First we clone the map and clear the old one
                val pendingGuildsClone = mutex.withLock {
                    val clonedMap = pendingGuilds.toMap()
                    pendingGuilds.clear()
                    clonedMap
                }

                val guildIds = pendingGuildsClone.keys

                // No need to process if the guild map is empty
                if (guildIds.isEmpty())
                    return@launch

                // Everything is good? Great! Let's prepare all guilds then!
                val serverConfigs = loritta.newSuspendedTransaction {
                    // Workaround to avoid "PreparedStatement can have at most 65,535 parameters" issue
                    guildIds.chunked(65_535).flatMap {
                        ServerConfig.find {
                            ServerConfigs.id inList it
                        }.toList()
                    }
                }

                logger.info { "Preparing ${guildIds.size} guilds with ${serverConfigs.size} server configs" }
                val start = System.currentTimeMillis()

                // And after getting all serverConfigs, we now can set up the guild!
                val allJobs = mutableListOf<Deferred<Unit>>()

                for (serverConfig in serverConfigs) {
                    val guild = pendingGuildsClone[serverConfig.id.value]

                    if (guild != null)
                        allJobs.add(setupGuild(guild, serverConfig))
                }

                allJobs.forEach {
                    try {
                        it.await()
                    } catch (e: Exception) {
                        logger.warn(e) { "Exception while preparing guild $guild!" }
                    }
                }

                creatingJobMutex.withLock {
                    job = null
                }

                logger.info { "Done! ${guildIds.size} guilds with ${serverConfigs.size} server configs were set up! Let's roll!! Took ${System.currentTimeMillis() - start}ms" }
            }
        }
    }

    private suspend fun setupGuild(guild: Guild, serverConfig: ServerConfig): Deferred<Unit> {
        return GlobalScope.async(loritta.coroutineDispatcher) {
            val mutes = loritta.newSuspendedTransaction {
                Mute.find {
                    (Mutes.isTemporary eq true) and (Mutes.guildId eq guild.idLong)
                }.toMutableList()
            }

            for (mute in mutes) {
                val member = guild.retrieveMemberOrNullById(mute.userId) ?: continue

                logger.info("Adicionado removal thread pelo MutedUsersThread já que a guild iniciou! ~ Guild: ${mute.guildId} - User: ${mute.userId}")
                MuteCommand.spawnRoleRemovalThread(loritta, guild, loritta.localeManager.getLocaleById(serverConfig.localeId), member.user, mute.expiresAt!!)
            }

            val allActiveGiveaways = loritta.newSuspendedTransaction {
                Giveaway.find { (Giveaways.guildId eq guild.idLong) and (Giveaways.finished eq false) }.toMutableList()
            }

            allActiveGiveaways.forEach {
                try {
                    if (loritta.giveawayManager.giveawayTasks[it.id.value] == null)
                        loritta.giveawayManager.createGiveawayJob(it)
                } catch (e: Exception) {
                    logger.error(e) { "Error while creating giveaway ${it.id.value} job on guild ready ${guild.idLong}" }
                }
            }
        }
    }

    private inline fun <K, V> MutableMap<K, V>.getOrPutNullable(key: K, defaultValue: () -> V): V {
        return if (!containsKey(key)) {
            val answer = defaultValue()
            put(key, answer)
            answer
        } else {
            @Suppress("UNCHECKED_CAST")
            get(key) as V
        }
    }
}