package net.perfectdreams.loritta.dao

import net.perfectdreams.loritta.tables.ReactionOptions
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class ReactionOption(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ReactionOption>(ReactionOptions)

    var guildId by ReactionOptions.guildId
    var textChannelId by ReactionOptions.textChannelId
    var messageId by ReactionOptions.messageId
    var reaction by ReactionOptions.reaction
    var roleIds by ReactionOptions.roleIds
    var locks by ReactionOptions.locks
}