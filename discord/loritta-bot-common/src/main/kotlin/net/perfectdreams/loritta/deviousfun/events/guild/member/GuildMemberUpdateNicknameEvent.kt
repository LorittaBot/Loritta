package net.perfectdreams.loritta.deviousfun.events.guild.member

import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.entities.Member
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.deviousfun.events.guild.GuildEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import java.time.OffsetDateTime

class GuildMemberUpdateNicknameEvent(
    deviousFun: DeviousFun,
    gateway: DeviousGateway,
    guild: Guild,
    val user: User,
    val member: Member,
    val oldNickname: String?,
    val newNickname: String?
) : GuildEvent(deviousFun, gateway, guild)