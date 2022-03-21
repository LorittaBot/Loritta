package net.perfectdreams.loritta.cinnamon.pudding.tables

object CachedDiscordUsers : SnowflakeTable() {
    val name = text("name").index()
    val discriminator = text("discriminator").index()
    val avatarId = text("avatar_id").nullable()
    val createdAt = long("created_at").index()
    val updatedAt = long("updated_at").index()
}