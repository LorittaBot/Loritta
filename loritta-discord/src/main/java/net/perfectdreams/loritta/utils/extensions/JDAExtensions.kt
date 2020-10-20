package net.perfectdreams.loritta.utils.extensions

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.utils.ImageFormat

/**
 * Gets the effective avatar URL in the specified [format]
 *
 * @see getEffectiveAvatarUrl
 */
fun User.getEffectiveAvatarUrl(format: ImageFormat) = getEffectiveAvatarUrl(format, 128)

/**
 * Gets the effective avatar URL in the specified [format] and [ímageSize]
 *
 * @see getEffectiveAvatarUrlInFormat
 */
fun User.getEffectiveAvatarUrl(format: ImageFormat, imageSize: Int): String {
    val extension = format.extension

    return if (avatarId != null) {
        "https://cdn.discordapp.com/avatars/$id/$avatarId.${extension}?size=$imageSize"
    } else {
        val avatarId = idLong % 5
        // This only exists in png AND doesn't have any other sizes
        "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
    }
}