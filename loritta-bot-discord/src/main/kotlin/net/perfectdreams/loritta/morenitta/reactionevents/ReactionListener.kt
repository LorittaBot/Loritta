package net.perfectdreams.loritta.morenitta.reactionevents

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.CollectedReactionEventPoints
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.ReactionEventDrops
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.ReactionEventPlayers
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ReactionListener(val m: LorittaBot) : ListenerAdapter() {
    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (!event.isFromGuild)
            return

        if (event.user?.isBot == true)
            return

        val userId = event.userIdLong
        val guild = event.guild
        val emoji = event.reaction.emoji
        val now = Instant.now()
        val activeEvent = ReactionEventsAttributes.getActiveEvent(now) ?: return  // No active event, bail out!
        val reactionSet = activeEvent.reactionSets.firstOrNull { emoji == m.emojiManager.get(it.reaction).toJDA() } ?: return // Unknown reaction set, bail out!

        // println("[ReactionListener] Reaction add")

        GlobalScope.launch(m.coroutineDispatcher) {
            val lorittaProfile = m.getLorittaProfile(userId) ?: return@launch

            m.newSuspendedTransaction {
                val playerData = ReactionEventPlayers.selectAll()
                    .where {
                        ReactionEventPlayers.userId eq lorittaProfile.id.value and (ReactionEventPlayers.event eq activeEvent.internalId) and (ReactionEventPlayers.leftAt.isNull())
                    }.firstOrNull()

                // Bye
                if (playerData == null)
                    return@newSuspendedTransaction

                // Does this message have any drop in it? (and does it have a drop with the proper reaction set ID?)
                val dropData = ReactionEventDrops.selectAll().where {
                    ReactionEventDrops.messageId eq event.messageIdLong and (ReactionEventDrops.reactionSetId eq reactionSet.reactionSetId)
                }.firstOrNull()

                // println("[ReactionListener] Drop data: $dropData")

                // Bye²
                if (dropData == null || dropData[ReactionEventDrops.createdAt].isBefore(now.minusMillis(900_000)))
                    return@newSuspendedTransaction

                // We technically don't need to check if the ID matches, because if it doesn't match, that would already be checked when querying the dropData

                val hasGotTheDrop = (CollectedReactionEventPoints innerJoin ReactionEventDrops).selectAll()
                    .where {
                        CollectedReactionEventPoints.user eq playerData[ReactionEventPlayers.id] and (ReactionEventDrops.messageId eq event.messageIdLong) and (ReactionEventDrops.reactionSetId eq reactionSet.reactionSetId)
                    }.firstOrNull()

                // println("[ReactionListener] hasGotTheDrop: $hasGotTheDrop")

                // Bye³
                if (hasGotTheDrop != null)
                    return@newSuspendedTransaction

                // Insert the collected point
                CollectedReactionEventPoints.insert {
                    it[CollectedReactionEventPoints.user] = playerData[ReactionEventPlayers.id]
                    it[CollectedReactionEventPoints.drop] = dropData[ReactionEventDrops.id]
                    it[CollectedReactionEventPoints.points] = reactionSet.pointsPayout
                    it[CollectedReactionEventPoints.collectedAt] = Instant.now()
                    it[CollectedReactionEventPoints.valid] = true
                }
            }
        }
    }
}