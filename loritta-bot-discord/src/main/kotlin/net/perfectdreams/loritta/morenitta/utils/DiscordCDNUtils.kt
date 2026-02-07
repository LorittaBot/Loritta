package net.perfectdreams.loritta.morenitta.utils

import io.ktor.http.URLBuilder

object DiscordCDNUtils {
    /**
     * Gets the effective avatar URL for the user [userId] with the [avatarId] in the specified [format] and [imageSize]
     *
     * @param userId the user ID
     * @param avatarId the avatar ID
     * @param format the image format, if null, the format will be falled back to webp for animated avatars and png for static avatars
     * @param imageSize the desired image size, if null, the default size will be used
     */
    fun getEffectiveAvatarUrl(userId: Long, avatarId: String?, format: ImageFormat?, imageSize: Int?): String {
        return if (avatarId != null) {
            getEffectiveAvatarUrl(userId, avatarId, format, imageSize)
        } else {
            getDefaultAvatarUrl(userId)
        }
    }

    /**
     * Gets the avatar URL for the user [userId] with the [avatarId] in the specified [format] and [imageSize]
     *
     * @param userId the user ID
     * @param avatarId the avatar ID
     * @param format the image format, if null, the format will be falled back to webp for animated avatars and png for static avatars
     * @param imageSize the desired image size, if null, the default size will be used
     */
    fun getAvatarUrl(userId: Long, avatarId: String, format: ImageFormat?, imageSize: Int?): String {
        val isAnimated = avatarId.startsWith("a_")
        val extension = format ?: if (isAnimated) ImageFormat.WEBP else ImageFormat.PNG

        // Discord avatars start with "a_"
        // Since ~06/02/2026, animated webp avatars CANNOT be transcoded to GIFs
        return URLBuilder("https://cdn.discordapp.com/avatars/${userId}/${avatarId}.${extension}").apply {
            if (isAnimated && extension.supportsAnimation)
                parameters.append("animated", "true")
            if (imageSize != null)
                parameters.append("size", imageSize.toString())
        }.toString()
    }

    /**
     * Gets the default avatar URL for the user [userId]
     *
     * @param userId the user ID
     */
    fun getDefaultAvatarUrl(userId: Long): String {
        val avatarId = userId % 5
        // This only exists in png AND doesn't have any other sizes
        return "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
    }
}