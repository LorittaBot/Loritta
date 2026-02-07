package net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda

import com.fasterxml.jackson.annotation.JsonIgnore
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordUser
import net.perfectdreams.loritta.morenitta.utils.DiscordCDNUtils
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.extensions.getEffectiveAvatarUrl

open class JDAUser(@JsonIgnore val handle: net.dv8tion.jda.api.entities.User) : DiscordUser {
    override val id: Long
        get() = handle.idLong

    override val name: String
        get() = handle.name

    override val avatar: String?
        get() = handle.avatarId

    override val avatarUrl: String?
        get() = handle.effectiveAvatarUrl

    override val asMention: String
        get() = handle.asMention

    override val isBot: Boolean
        get() = handle.isBot

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
        return DiscordCDNUtils.getEffectiveAvatarUrl(this.id, this.avatar, format, imageSize)
    }
}