package net.perfectdreams.loritta.morenitta.utils

import dev.minn.jda.ktx.coroutines.await
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import org.jetbrains.exposed.sql.and
import java.util.*

object UserUtils {
    suspend fun fillUsersFromRecentMessages(
        context: UnleashedContext,
        users: List<net.dv8tion.jda.api.entities.User?>
    ): UserFillResult {
        val targetSize = users.size
        var noPermissionToQuery = false
        val usersToBeFilled = users.toMutableList()
        val guild = context.guildOrNull

        // Are in a guild? Because if yes, we can do more :sparkles: crazy :sparkles: stuff to fill the users
        if (guild != null) {
            val guildId = guild.idLong

            if (users.filterNotNull().size != targetSize) {
                // Get random users from chat
                try {
                    val currentNotNullUserIds = users.filterNotNull().map { it.id }

                    val messages = context.channel.history.retrievePast(100).await()

                    // We shuffle the array to avoid users using the same command a lot of times... just to be bored because all the responses are (almost) the same
                    // We also remove any users that are already present in the listOfUsers list
                    val uniqueUsers = messages
                        .asSequence()
                        .map { it.author }
                        .distinctBy { it.id }
                        .filter { it.id !in currentNotNullUserIds }
                        .shuffled()
                        .toList()

                    val uniqueNonBotUsers = LinkedList(uniqueUsers.filter { !it.isBot })
                    val uniqueBotUsers = LinkedList(uniqueUsers.filter { it.isBot })

                    // First we will get non bot users, because users love complaining that "but I don't want to have bots on my sad reality meme!! bwaaa!!"
                    while (usersToBeFilled.filterNotNull().size != targetSize && uniqueNonBotUsers.isNotEmpty()) {
                        val indexOfFirstNullEntry = usersToBeFilled.indexOf(null)
                        usersToBeFilled[indexOfFirstNullEntry] = uniqueNonBotUsers.poll()
                    }

                    // If we still haven't found it, we will query bot users so the user can at least have a sad reality instead of a "couldn't find enough users" message
                    while (usersToBeFilled.filterNotNull().size != targetSize && uniqueBotUsers.isNotEmpty()) {
                        val indexOfFirstNullEntry = usersToBeFilled.indexOf(null)
                        usersToBeFilled[indexOfFirstNullEntry] = uniqueBotUsers.poll()
                    }
                } catch (e: Exception) {
                    // No permission to query!
                    noPermissionToQuery = true
                }
            }

            // Okay, so it is still not filled, well...
            if (users.filterNotNull().size != targetSize) {
                // What we can do is pull from the GuildProfiles!
                val usersInTheGuild = context.loritta.pudding.transaction {
                    GuildProfiles.select(GuildProfiles.userId)
                        .where { GuildProfiles.guildId eq guildId.toLong() and (GuildProfiles.isInGuild eq true) }
                        .map { it[GuildProfiles.userId] }
                        .filter { it !in usersToBeFilled.mapNotNull { it?.id?.toLong() } } // Ignore users that are already in the list
                        .shuffled()
                        .let { LinkedList(it) }
                }

                // From that, we will try filling out the null information
                while (usersToBeFilled.filterNotNull().size != targetSize && usersInTheGuild.isNotEmpty()) {
                    val indexOfFirstNullEntry = usersToBeFilled.indexOf(null)
                    val userId = usersInTheGuild.poll()

                    try {
                        val user = context.loritta.lorittaShards.retrieveUserById(userId)
                        usersToBeFilled[indexOfFirstNullEntry] = user
                    } catch (e: Exception) {
                        // The user doesn't exist anymore! (Maybe?)
                    }
                }
            }

            // Hmm, now we are in the "This is the last shot" territory
            if (users.filterNotNull().size != targetSize) {
                // If it is a guild context, we can try pulling random users from the server!
                val members = guild.members
                    .filter { it.user.idLong !in usersToBeFilled.mapNotNull { it?.idLong } } // Ignore users that are already in the list
                    .shuffled() // Shuffle the list to avoid the same results every single time...
                    .let { LinkedList(it) }

                while (usersToBeFilled.filterNotNull().size != targetSize && members.isNotEmpty()) {
                    val indexOfFirstNullEntry = usersToBeFilled.indexOf(null)
                    // The user shouldn't be null here... well, I hope so
                    usersToBeFilled[indexOfFirstNullEntry] = members.poll().user
                }
            }
        }

        val nonNullUsers = usersToBeFilled.filterNotNull()

        return UserFillResult(
            nonNullUsers,
            nonNullUsers.size == targetSize,
            noPermissionToQuery
        )
    }

    data class UserFillResult(
        val users: List<net.dv8tion.jda.api.entities.User>,
        val successfullyFilled: Boolean,
        val noPermissionToQuery: Boolean
    )
}