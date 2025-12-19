package net.perfectdreams.dora

data class Translator(
    val id: Long,
    val discordId: Long,
    val name: String,
    val avatarId: String?
) {
    val avatarUrl: String?
        get() {
            return if (avatarId != null) {
                val extension = if (avatarId.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
                    "gif"
                } else { "png" }

                "https://cdn.discordapp.com/avatars/${id}/${avatarId}.${extension}"
            } else null
        }

    val defaultAvatarUrl: String
        get() {
            val avatarId = id % 5

            return "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
        }

    val effectiveAvatarUrl: String
        get() {
            return avatarUrl ?: defaultAvatarUrl
        }
}