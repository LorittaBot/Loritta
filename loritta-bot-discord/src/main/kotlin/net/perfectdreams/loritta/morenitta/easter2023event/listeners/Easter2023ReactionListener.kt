package net.perfectdreams.loritta.morenitta.easter2023event.listeners

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.CollectedEaster2023Eggs
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.Easter2023Drops
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.Easter2023Players
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.easter2023event.LorittaEaster2023Event
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class Easter2023ReactionListener(val m: LorittaBot) : ListenerAdapter() {
    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (!event.isFromGuild)
            return

        if (event.user?.isBot == true)
            return

        val userId = event.userIdLong
        val guild = event.guild
        val emoji = event.reaction.emoji

        if (emoji !is CustomEmoji)
            return

        val eggEmoji = LorittaEaster2023Event.emojiToEasterEggColor(emoji) ?: return

        if (!LorittaEaster2023Event.isEventActive())
            return

        GlobalScope.launch(m.coroutineDispatcher) {
            val lorittaProfile = m.getLorittaProfile(userId) ?: return@launch

            m.newSuspendedTransaction {
                val isParticipating = Easter2023Players.selectAll().where {
                    Easter2023Players.id eq lorittaProfile.id
                }.count() != 0L

                // Bye
                if (!isParticipating)
                    return@newSuspendedTransaction

                // Does this message have any drop in it?
                val dropData = Easter2023Drops.selectAll().where {
                    Easter2023Drops.messageId eq event.messageIdLong
                }.firstOrNull()

                // Bye²
                if (dropData == null || dropData[Easter2023Drops.createdAt].isBefore(Instant.now().minusMillis(900_000)))
                    return@newSuspendedTransaction

                val hasGotTheDrop = (CollectedEaster2023Eggs innerJoin Easter2023Drops).selectAll().where {
                    CollectedEaster2023Eggs.user eq event.userIdLong and
                            (Easter2023Drops.messageId eq event.messageIdLong)
                }.firstOrNull()

                // Bye³
                if (hasGotTheDrop != null)
                    return@newSuspendedTransaction

                // Insert the collected egg
                CollectedEaster2023Eggs.insert {
                    it[user] = lorittaProfile.id
                    it[message] = dropData[Easter2023Drops.id]
                    it[points] = 1
                    it[collectedAt] = Instant.now()
                    it[valid] = true
                }
            }
        }
    }
}