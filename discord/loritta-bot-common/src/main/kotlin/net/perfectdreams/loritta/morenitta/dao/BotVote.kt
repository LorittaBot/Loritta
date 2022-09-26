package net.perfectdreams.loritta.morenitta.dao

import net.perfectdreams.loritta.morenitta.tables.BotVotes
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class BotVote(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<BotVote>(BotVotes)

    var userId by BotVotes.userId
    var websiteSource by BotVotes.websiteSource
    var votedAt by BotVotes.votedAt
}