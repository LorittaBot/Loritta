package net.perfectdreams.loritta.morenitta.dao

import net.perfectdreams.loritta.cinnamon.pudding.tables.StarboardMessages
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class StarboardMessage(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<StarboardMessage>(StarboardMessages)

	var guildId by StarboardMessages.guildId
	var embedId by StarboardMessages.embedId
	var messageId by StarboardMessages.messageId
}