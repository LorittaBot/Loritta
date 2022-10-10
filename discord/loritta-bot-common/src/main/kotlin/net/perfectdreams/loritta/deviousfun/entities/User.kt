package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.UserFlags
import dev.kord.rest.json.request.DMCreateRequest
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.cache.DeviousChannelData
import net.perfectdreams.loritta.deviousfun.cache.DeviousUserData
import net.perfectdreams.loritta.morenitta.utils.ImageFormat

class User(val deviousFun: DeviousFun, override val idSnowflake: Snowflake, val discordUser: DeviousUserData) : Mentionable, IdentifiableSnowflake {
    val name: String
        get() = discordUser.username
    val discriminator: String
        get() = discordUser.discriminator
    val effectiveAvatarUrl: String
        get() = avatarUrl ?: defaultAvatarUrl
    val defaultAvatarUrl: String
        get() = "https://cdn.discordapp.com/embed/avatars/${discriminator.toInt() % 4}.png"
    val avatarId: String?
        get() = discordUser.avatar
    val avatarUrl: String?
        get() = avatarId?.let {
            "https://cdn.discordapp.com/avatars/$idSnowflake/$avatarId.${if (it.startsWith("a_")) "gif" else "png"}"
        }
    val isBot: Boolean
        get() = discordUser.bot
    val flagsRaw: Int
        get() = discordUser.flags.code
    val flags: UserFlags
        get() = discordUser.flags
    val asTag: String
        get() = "$name#$discriminator"
    override val asMention: String
        get() = "<@${idSnowflake}>"

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

        return if (avatarId != null) {
            "https://cdn.discordapp.com/avatars/$id/$avatarId.${extension}?size=$imageSize"
        } else {
            val avatarId = idLong % 5
            // This only exists in png AND doesn't have any other sizes
            "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
        }
    }

    suspend fun openPrivateChannel(): Channel {
        val privateChannel = deviousFun.loritta.rest.user.createDM(DMCreateRequest(idSnowflake))
        return Channel(deviousFun, null, DeviousChannelData.from(null, privateChannel))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is User)
            return false

        return this.idSnowflake == other.idSnowflake
    }
}