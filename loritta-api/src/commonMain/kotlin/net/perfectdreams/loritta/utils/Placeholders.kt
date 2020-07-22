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

    object Deprecated {
        val USER_ID = LorittaPlaceholder("user-id")
        val USER_DISCRIMINATOR = LorittaPlaceholder("user-discriminator")
        val USER_NICKNAME = LorittaPlaceholder("nickname")
        val USER_AVATAR_URL = LorittaPlaceholder("user-avatar-url")
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