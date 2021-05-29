package net.perfectdreams.loritta.platform.discord.legacy.entities.jda

import com.fasterxml.jackson.annotation.JsonIgnore
import net.perfectdreams.loritta.platform.discord.legacy.entities.DiscordUser
import net.perfectdreams.loritta.utils.ImageFormat
import net.perfectdreams.loritta.utils.extensions.getEffectiveAvatarUrl

open class JDAUser(@JsonIgnore val handle: net.dv8tion.jda.api.entities.User) : DiscordUser {
    override val id: Long
        get() = handle.idLong

    override val name: String
        get() = handle.name

    override val avatar: String?
        get() = handle.avatarId

    override val avatarUrl: String?
        get() = handle.effectiveAvatarUrl

    override val effectiveAvatarUrl: String
        get() = handle.effectiveAvatarUrl

    override val defaultAvatarUrl: String
        get() = handle.defaultAvatarUrl

    override val asMention: String
        get() = handle.asMention

    override val isBot: Boolean
        get() = handle.isBot

    override val discriminator: String
        get() = handle.discriminator

    /**
     * Gets the effective avatar URL in the specified [format]
     *
     * @see getEffectiveAvatarUrl
     */
    fun getEffectiveAvatarUrl(format: ImageFormat) = getEffectiveAvatarUrl(format, 128)

    /**
     * Gets the effective avatar URL in the specified [format] and [ímageSize]
     *
     * @see getEffectiveAvatarUrlInFormat
     */
    fun getEffectiveAvatarUrl(format: ImageFormat, imageSize: Int): String {
        val extension = format.extension

        return if (avatar != null) {
            "https://cdn.discordapp.com/avatars/$id/$avatar.${extension}?size=$imageSize"
        } else {
            val avatarId = id % 5
            // This only exists in png AND doesn't have any other sizes
            "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
        }
    }
}