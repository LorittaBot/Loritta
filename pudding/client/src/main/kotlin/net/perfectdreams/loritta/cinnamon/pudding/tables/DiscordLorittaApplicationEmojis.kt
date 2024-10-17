package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object DiscordLorittaApplicationEmojis : LongIdTable() {
    val emojiName = text("emoji_name").uniqueIndex()
    val emojiId = long("emoji_id")
    val animated = bool("animated")
    val imageHash = binary("image_hash")
}