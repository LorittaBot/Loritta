package net.perfectdreams.loritta.placeholders

object Placeholders {
    // ===[ USER ]===
    val USER_MENTION = LorittaPlaceholder("@user", false)
    val USER_NAME_SHORT = LorittaPlaceholder("user", false)
    val USER_NAME = LorittaPlaceholder("user.name", false)
    val USER_DISCRIMINATOR = LorittaPlaceholder("user.discriminator", false)
    val USER_TAG = LorittaPlaceholder("user.tag", false)
    val USER_ID = LorittaPlaceholder("user.id", false)
    val USER_AVATAR_URL = LorittaPlaceholder("user.avatar", false)
    val USER_NICKNAME = LorittaPlaceholder("user.nickname", false)

    // ===[ GUILD ]===
    val GUILD_NAME_SHORT = LorittaPlaceholder("guild", false)
    val GUILD_NAME = LorittaPlaceholder("guild.name", false)
    val GUILD_SIZE = LorittaPlaceholder("guild.size", false)
    val GUILD_ICON_URL = LorittaPlaceholder("guild.icon", false)

    // ===[ EXPERIENCE ]===
    private const val EXPERIENCE_PREFIX = "experience"
    private const val EXPERIENCE_NEXT_LEVEL_PREFIX = "$EXPERIENCE_PREFIX.next-level"

    val EXPERIENCE_LEVEL_SHORT = LorittaPlaceholder("level", true)
    val EXPERIENCE_XP_SHORT = LorittaPlaceholder("xp", true)

    val EXPERIENCE_LEVEL = LorittaPlaceholder("$EXPERIENCE_PREFIX.level", false)
    val EXPERIENCE_XP = LorittaPlaceholder("$EXPERIENCE_PREFIX.xp", false)

    val EXPERIENCE_RANKING = LorittaPlaceholder("$EXPERIENCE_PREFIX.ranking", false)

    val EXPERIENCE_NEXT_LEVEL = LorittaPlaceholder(EXPERIENCE_NEXT_LEVEL_PREFIX, false)
    val EXPERIENCE_NEXT_LEVEL_REQUIRED_XP = LorittaPlaceholder("$EXPERIENCE_NEXT_LEVEL_PREFIX.required-xp", false)
    val EXPERIENCE_NEXT_LEVEL_TOTAL_XP = LorittaPlaceholder("$EXPERIENCE_NEXT_LEVEL_PREFIX.total-xp", false)
    val EXPERIENCE_NEXT_ROLE_REWARD = LorittaPlaceholder("$EXPERIENCE_PREFIX.next-role-reward", false)

    // ===[ PUNISHMENT ]===
    private const val PUNISHMENT_PREFIX = "punishment"
    private const val STAFF_PREFIX = "staff"

    val PUNISHMENT_REASON = LorittaPlaceholder("$PUNISHMENT_PREFIX.reason", false)
    val PUNISHMENT_TYPE = LorittaPlaceholder("$PUNISHMENT_PREFIX.type", false)
    val PUNISHMENT_REASON_SHORT = LorittaPlaceholder("reason", false)
    val PUNISHMENT_TYPE_SHORT = LorittaPlaceholder("punishment", false)
    val PUNISHMENT_DURATION = LorittaPlaceholder("duration", false)

    // ===[ MODERATION ]===
    val STAFF_MENTION = LorittaPlaceholder("@staff", false)
    val STAFF_NAME_SHORT = LorittaPlaceholder("staff", false)
    val STAFF_NAME = LorittaPlaceholder("$STAFF_PREFIX.name", false)
    val STAFF_DISCRIMINATOR = LorittaPlaceholder("$STAFF_PREFIX.discriminator", false)
    val STAFF_TAG = LorittaPlaceholder("$STAFF_PREFIX.tag", false)
    val STAFF_ID = LorittaPlaceholder("$STAFF_PREFIX.id", false)
    val STAFF_AVATAR_URL = LorittaPlaceholder("$STAFF_PREFIX.avatar", false)
    val STAFF_NICKNAME = LorittaPlaceholder("$STAFF_PREFIX.nickname", false)

    val LINK = LorittaPlaceholder("link", false)

    // ===[ TWITCH ]===
    val STREAM_TITLE = LorittaPlaceholder("stream.title", false)
    val STREAM_URL = LorittaPlaceholder("stream.url", false)
    val STREAM_GAME = LorittaPlaceholder("stream.game", false)

    // ===[ YOUTUBE ]===
    val VIDEO_TITLE = LorittaPlaceholder("video.title", false)
    val VIDEO_ID = LorittaPlaceholder("video.id", false)
    val VIDEO_URL = LorittaPlaceholder("video.url", false)
    val VIDEO_THUMBNAIL = LorittaPlaceholder("video.thumbnail", false)

    // ===[ BLUESKY ]===
    val BLUESKY_POST_URL = LorittaPlaceholder("post.url", false)

    // ===[ DAILY SHOP TRINKETS ]===
    val DAILY_SHOP_DATE_SHORT = LorittaPlaceholder("daily-shop.date-short", false)

    object Deprecated {
        val USER_ID = LorittaPlaceholder("user-id", true)
        val USER_DISCRIMINATOR = LorittaPlaceholder("user-discriminator", true)
        val USER_NICKNAME = LorittaPlaceholder("nickname", true)
        val USER_AVATAR_URL = LorittaPlaceholder("user-avatar-url", true)

        // ===[ PUNISHMENT ]===
        val STAFF_DISCRIMINATOR = LorittaPlaceholder("staff-discriminator", true)
        val STAFF_ID = LorittaPlaceholder("staff-id", true)
        val STAFF_AVATAR_URL = LorittaPlaceholder("staff-avatar-url", true)

        // ===[ GUILD ]===
        val GUILD_SIZE = LorittaPlaceholder("guild-size", true)
        val GUILD_SIZE_JOINED = LorittaPlaceholder("guildsize", true)
        val GUILD_ICON_URL = LorittaPlaceholder("guild-icon-url", true)

        // ===[ YOUTUBE ]===
        val VIDEO_TITLE = LorittaPlaceholder("title", true)
        val VIDEO_TITLE_BR = LorittaPlaceholder("título", true)
        val VIDEO_ID = LorittaPlaceholder("video-id", true)
        val VIDEO_URL = LorittaPlaceholder("link", true)
    }
}