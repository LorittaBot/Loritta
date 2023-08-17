package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object CachedYouTubeChannelIds : IdTable<String>() {
	val channelId = text("channel")
	override val id: Column<EntityID<String>> = channelId.entityId()

	val title = text("title")
	val avatarUrl = text("avatar_url")
	val retrievedAt = long("retrieved_at")

	override val primaryKey = PrimaryKey(channelId)
}