package net.perfectdreams.loritta.morenitta.websitedashboard

data class UserSession(
    val websiteToken: String,
    val discordAccessToken: String,
    val userId: Long,
    val username: String,
    val discriminator: String,
    val globalName: String?,
    val avatarId: String?
) {
    fun getEffectiveAvatarUrl(): String {
        val userAvatarId = this.avatarId

        val avatarUrl = if (userAvatarId != null) {
            val extension = if (userAvatarId.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
                "gif"
            } else {
                "png"
            }

            "https://cdn.discordapp.com/avatars/${this.userId}/${userAvatarId}.${extension}?size=64"
        } else {
            val avatarId = (this.userId shr 22) % 6

            "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
        }

        return avatarUrl
    }
}