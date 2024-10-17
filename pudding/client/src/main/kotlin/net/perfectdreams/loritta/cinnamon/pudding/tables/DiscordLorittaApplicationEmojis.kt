package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object DiscordLorittaApplicationEmojis : IdTable<String>() {
    override val id: Column<EntityID<String>> = text("emoji_name").uniqueIndex().entityId()

    val emojiId = long("emoji_id")
    val animated = bool("animated")
    val imageHash = binary("image_hash")
}