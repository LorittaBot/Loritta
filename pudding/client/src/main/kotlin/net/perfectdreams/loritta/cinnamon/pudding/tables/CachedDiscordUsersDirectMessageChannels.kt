package net.perfectdreams.loritta.cinnamon.pudding.tables

object CachedDiscordUsersDirectMessageChannels : SnowflakeTable() {
    val channelId = long("channel_id")
}