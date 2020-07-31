package net.perfectdreams.loritta.parallax.api

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.parallax.api.packet.ParallaxConnectionUtils
import net.perfectdreams.loritta.parallax.api.packet.ParallaxDeleteRolePacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxPutRolePacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxSendMessagePacket

@Serializable
class ParallaxGuild(
        val id: Long,
        val name: String,
        val members: List<ParallaxMember>,
        val roles: List<ParallaxRole>
) {
    fun getRoleById(id: Long) = roles.firstOrNull { it.id == id }
    fun getRoleById(id: String) = getRoleById(id.toLong())

    fun addRoleToMember(member: ParallaxMember, role: ParallaxRole) {
        val sendMessage = ParallaxPutRolePacket(
                member.user.id,
                role.id
        )

        return ParallaxConnectionUtils.sendPacket(sendMessage)
    }

    fun removeRoleFromMember(member: ParallaxMember, role: ParallaxRole) {
        val sendMessage = ParallaxDeleteRolePacket(
                member.user.id,
                role.id
        )

        return ParallaxConnectionUtils.sendPacket(sendMessage)
    }
}