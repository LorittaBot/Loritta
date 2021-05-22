package net.perfectdreams.loritta.platform.discord.utils

import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.dao.Mute
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.modules.ReactionModule
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNullById
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.loritta.dao.servers.Giveaway
import net.perfectdreams.loritta.dao.servers.moduleconfigs.ReactionOption
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.plugin.DiscordPlugin
import net.perfectdreams.loritta.tables.servers.Giveaways
import net.perfectdreams.loritta.tables.servers.moduleconfigs.ReactionOptions
import net.perfectdreams.loritta.utils.giveaway.GiveawayManager
import org.jetbrains.exposed.sql.and
import org.jetbrains.kotlin.utils.getOrPutNullable

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
class GuildSetupQueue(val loritta: LorittaDiscord) {
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
                    ServerConfig.find {
                        ServerConfigs.id inList guildIds
                    }.toList()
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
                MuteCommand.spawnRoleRemovalThread(guild, loritta.localeManager.getLocaleById(serverConfig.localeId), member.user, mute.expiresAt!!)
            }

            // Ao voltar, vamos reprocessar todas as reações necessárias do reaction role (desta guild)
            val reactionRoles = loritta.newSuspendedTransaction {
                ReactionOption.find { ReactionOptions.guildId eq guild.idLong }.toMutableList()
            }

            // Vamos fazer cache das mensagens para evitar pegando a mesma mensagem várias vezes
            val messages = mutableMapOf<Long, Message?>()

            for (option in reactionRoles) {
                val textChannel = guild.getTextChannelById(option.textChannelId) ?: continue
                val message = messages.getOrPutNullable(option.messageId) {
                    try {
                        textChannel.retrieveMessageById(option.messageId).await()
                    } catch (e: ErrorResponseException) {
                        null
                    }
                }

                messages[option.messageId] = message

                if (message == null)
                    continue

                // Verificar locks
                // Existem vários tipos de locks: Locks de opções (via ID), locks de mensagens (via... mensagens), etc.
                // Para ficar mais fácil, vamos verificar TODOS os locks da mensagem
                val locks = mutableListOf<ReactionOption>()

                for (lock in option.locks) {
                    if (lock.contains("-")) {
                        val split = lock.split("-")
                        val channelOptionLock = loritta.newSuspendedTransaction {
                            ReactionOption.find {
                                (ReactionOptions.guildId eq guild.idLong) and
                                        (ReactionOptions.textChannelId eq split[0].toLong()) and
                                        (ReactionOptions.messageId eq split[1].toLong())
                            }.toMutableList()
                        }
                        locks.addAll(channelOptionLock)
                    } else { // Lock por option ID, esse daqui é mais complicado!
                        val idOptionLock = loritta.newSuspendedTransaction {
                            ReactionOption.find {
                                (ReactionOptions.id eq lock.toLong())
                            }.toMutableList()
                        }
                        locks.addAll(idOptionLock)
                    }
                }

                // Agora nós já temos a opção desejada, só dar os cargos para o usuário!
                val roles = option.roleIds.mapNotNull { guild.getRoleById(it) }

                if (roles.isNotEmpty()) {
                    val reaction = message.reactions.firstOrNull {
                        it.reactionEmote.name == option.reaction || it.reactionEmote.emote.id == option.reaction
                    }

                    if (reaction != null) { // Reaction existe!
                        reaction.retrieveUsers().await().asSequence().filter { !it.isBot }.mapNotNull { guild.getMember(it) }.forEach {
                            ReactionModule.giveRolesToMember(it, reaction, option, locks, roles)
                        }
                    }
                }
            }

            val allActiveGiveaways = loritta.newSuspendedTransaction {
                Giveaway.find { (Giveaways.guildId eq guild.idLong) and (Giveaways.finished eq false) }.toMutableList()
            }

            allActiveGiveaways.forEach {
                try {
                    if (GiveawayManager.giveawayTasks[it.id.value] == null)
                        GiveawayManager.createGiveawayJob(it)
                } catch (e: Exception) {
                    logger.error(e) { "Error while creating giveaway ${it.id.value} job on guild ready ${guild.idLong}" }
                }
            }

            loritta.pluginManager.plugins.filterIsInstance(DiscordPlugin::class.java).flatMap {
                it.onGuildReadyListeners
            }.forEach {
                it.invoke(guild, serverConfig)
            }
        }
    }
}