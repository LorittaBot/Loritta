package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

// Used for stats
object DiscordGuilds : UniqueSnowflakeTable() {
    val name = text("name")
    val iconId = text("icon_id").nullable()
    val bannerId = text("banner_id").nullable()
    val splashId = text("splash_id").nullable()
    val ownerId = long("owner_id")
    val memberCount = integer("member_count").index()
    val joinedAt = timestampWithTimeZone("joined_at").index()
    val channelCount = integer("channel_count")
    val roleCount = integer("role_count")
    val emojiCount = integer("emoji_count")
    val stickerCount = integer("sticker_count")
    val boostCount = integer("boost_count")
    val vanityCode = text("vanity_code").nullable()
    val verificationLevel = integer("verification_level")
    val nsfwLevel = integer("nsfw_level")
    val explicitContentLevel = integer("explicit_content_level")
    val features = array<String>("features")
    val locale = text("locale").index()
    val clusterId = integer("cluster_id").index()
    val shardId = integer("shard_id").index()
    val lastUpdatedAt = timestampWithTimeZone("last_updated_at")
}