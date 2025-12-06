package net.perfectdreams.loritta.helper.utils.tickets

import dev.minn.jda.ktx.generics.getChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.perfectdreams.loritta.helper.utils.extensions.await

class TicketsCache(
    val jda: JDA,
    val guildId: Long,
    val channelId: Long
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val tickets = mutableMapOf<Long, DiscordThreadTicketData>()
    // Avoids dupe threads being created when the cache is not populated yet
    val mutex = Mutex()

    // We cache tickets so tickets can be created faster, because if we have too many archived tickets, when a user that never created a ticket before tries
    // opening a ticket, it takes a looong time (1+ minute) just to create it, which is kinda bad for UX
    suspend fun populateCache() {
        mutex.withLock {
            val guild = jda.getGuildById(guildId)

            if (guild == null) {
                logger.warn { "Guild $guildId not found! Bug?" }
                return@withLock
            }

            val channel = guild.getTextChannelById(channelId)

            if (channel == null) {
                logger.warn { "Channel $channelId not found in guild $guildId! Bug?" }
                return@withLock
            }

            // Populate cache with the active threads
            guild
                .retrieveActiveThreads()
                .await()
                .forEach {
                    if (it.parentChannel == channel) {
                        val name = it.name
                        if (!name.contains("(") && !name.contains(")"))
                            return@forEach

                        val onlyTheId = name.substringAfterLast("(").substringBeforeLast(")")
                        val userIdAsLong = onlyTheId.toLongOrNull() ?: return@forEach

                        tickets[userIdAsLong] = DiscordThreadTicketData(it.idLong)
                    }
                }


            // Populate cache with the inactive threads
            val paginationAction = channel.retrieveArchivedPrivateThreadChannels()
                // Increase limit from 5 (Discord Default) to 100
                .limit(100)
            var currentLastKey = paginationAction.lastKey

            while (true) {
                val threads = paginationAction.await()

                threads.forEach {
                    val name = it.name ?: return@forEach
                    if (!name.contains("(") && !name.contains(")"))
                        return@forEach

                    val onlyTheId = name.substringAfterLast("(").substringBeforeLast(")")
                    val userIdAsLong = onlyTheId.toLongOrNull() ?: return@forEach

                    tickets[userIdAsLong] = DiscordThreadTicketData(it.idLong)
                }

                if (paginationAction.lastKey == currentLastKey)
                    break

                currentLastKey = paginationAction.lastKey
            }
        }
    }

    data class DiscordThreadTicketData(
        val id: Long
    )
}