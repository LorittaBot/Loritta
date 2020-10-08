package net.perfectdreams.loritta.utils

object Placeholders {
    val USER_MENTION = LorittaPlaceholder("@user")
    val USER_NAME_SHORT = LorittaPlaceholder("user")
    val USER_NAME = LorittaPlaceholder("user.name")
    val USER_DISCRIMINATOR = LorittaPlaceholder("user.discriminator")
    val USER_TAG = LorittaPlaceholder("user.tag")
    val USER_ID = LorittaPlaceholder("user.id")
    val USER_AVATAR_URL = LorittaPlaceholder("user.avatar")
    val USER_NICKNAME = LorittaPlaceholder("user.nickname")
    val LINK = LorittaPlaceholder("link")

    // ===[ EXPERIENCE ]===
    private const val EXPERIENCE_PREFIX = "experience"
    private const val EXPERIENCE_NEXT_LEVEL_PREFIX = "$EXPERIENCE_PREFIX.next-level"

    val EXPERIENCE_LEVEL_SHORT = LorittaPlaceholder("level")
    val EXPERIENCE_XP_SHORT = LorittaPlaceholder("xp")

    val EXPERIENCE_LEVEL = LorittaPlaceholder("$EXPERIENCE_PREFIX.level")
    val EXPERIENCE_XP = LorittaPlaceholder("$EXPERIENCE_PREFIX.xp")

    val EXPERIENCE_RANKING = LorittaPlaceholder("$EXPERIENCE_PREFIX.ranking")

    val EXPERIENCE_NEXT_LEVEL = LorittaPlaceholder(EXPERIENCE_NEXT_LEVEL_PREFIX)
    val EXPERIENCE_NEXT_LEVEL_REQUIRED_XP = LorittaPlaceholder("$EXPERIENCE_NEXT_LEVEL_PREFIX.required-xp")
    val EXPERIENCE_NEXT_LEVEL_TOTAL_XP = LorittaPlaceholder("$EXPERIENCE_NEXT_LEVEL_PREFIX.total-xp")

    // ===[ PUNISHMENT ]===
    private const val PUNISHMENT_PREFIX = "punishment"
    private const val STAFF_PREFIX = "staff"

    val PUNISHMENT_REASON = LorittaPlaceholder("$PUNISHMENT_PREFIX.reason")
    val PUNISHMENT_TYPE = LorittaPlaceholder("$PUNISHMENT_PREFIX.type")
    val PUNISHMENT_REASON_SHORT = LorittaPlaceholder("reason")
    val PUNISHMENT_TYPE_SHORT = LorittaPlaceholder("punishment")

    val STAFF_MENTION = LorittaPlaceholder("@staff")
    val STAFF_NAME_SHORT = LorittaPlaceholder("staff")
    val STAFF_NAME = LorittaPlaceholder("$STAFF_PREFIX.name")
    val STAFF_DISCRIMINATOR = LorittaPlaceholder("$STAFF_PREFIX.discriminator")
    val STAFF_TAG = LorittaPlaceholder("$STAFF_PREFIX.tag")
    val STAFF_ID = LorittaPlaceholder("$STAFF_PREFIX.id")
    val STAFF_AVATAR_URL = LorittaPlaceholder("$STAFF_PREFIX.avatar")
    // val STAFF_NICKNAME = LorittaPlaceholder("$STAFF_PREFIX.nickname")

    object Deprecated {
        val USER_ID = LorittaPlaceholder("user-id")
        val USER_DISCRIMINATOR = LorittaPlaceholder("user-discriminator")
        val USER_NICKNAME = LorittaPlaceholder("nickname")
        val USER_AVATAR_URL = LorittaPlaceholder("user-avatar-url")

        // ===[ PUNISHMENT ]===
        val STAFF_DISCRIMINATOR = LorittaPlaceholder("staff-discriminator")
        val STAFF_ID = LorittaPlaceholder("staff-id")
        val STAFF_AVATAR_URL = LorittaPlaceholder("staff-avatar-url")
    }

    /**
     * Creates a placeholder key for the [input] by wrapping it between {...}
     *
     * Example: If [input] is "@user", the returned value will be "{@user}"
     *
     * @param input the key
     * @return      the created placeholder key
     */
    fun createPlaceholderKey(input: String) = "{$input}"
}