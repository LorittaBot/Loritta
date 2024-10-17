package net.perfectdreams.loritta.cinnamon.pudding.tables

object DiscordLorittaApplicationEmojis : SnowflakeTable() {
    val emojiName = text("emoji_name")
    val imageHash = binary("image_hash")
}