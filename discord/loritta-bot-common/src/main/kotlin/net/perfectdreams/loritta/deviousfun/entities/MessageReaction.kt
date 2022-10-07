package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.rest.route.Position
import net.perfectdreams.loritta.deviousfun.JDA

class MessageReaction(
    val jda: JDA,
    val channelIdSnowflake: Snowflake,
    val messageIdSnowflake: Snowflake,
    val partialEmoji: DiscordPartialEmoji,
    val countOrNull: Int?
) {
    val count: Int
        get() = countOrNull ?: error("The reaction count is not available!")
    val reactionEmote: ReactionEmote
        get() = ReactionEmote(jda, partialEmoji)

    suspend fun retrieveUsers(count: Int = 100): MutableList<User> {
        var after = Snowflake.min
        val users = mutableListOf<User>()

        while (true) {
            val reactions = jda.loritta.rest.channel.getReactions(
                channelIdSnowflake,
                messageIdSnowflake,
                if (reactionEmote.isEmote) {
                    "${reactionEmote.name}:${reactionEmote.idSnowflake}"
                } else reactionEmote.name,
                limit = 100,
                after = Position.After(after)
            )

            users.addAll(
                reactions.map {
                    jda.cacheManager.createUser(it, true)
                }
            )

            if (count > reactions.size)
                break

            after = reactions.maxOf { it.id }
        }

        return users
    }

    suspend fun removeReaction(user: User) {
        jda.loritta.rest.channel.deleteReaction(channelIdSnowflake, messageIdSnowflake, user.idSnowflake, reactionEmote.name)
    }
}