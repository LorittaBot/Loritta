package net.perfectdreams.loritta.parallax.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.perfectdreams.loritta.api.entities.Guild

@Serializable
class ParallaxMember(
        val user: ParallaxUser,
        val nickname: String,
        val roleIds: List<Long>,
        val joinedAt: Long,
        val premiumSince: Long?
) {
    @Transient
    lateinit var guild: ParallaxGuild

    val roles: List<ParallaxRole>
        get() = guild.roles.filter {
            it.id in roleIds
        }
}