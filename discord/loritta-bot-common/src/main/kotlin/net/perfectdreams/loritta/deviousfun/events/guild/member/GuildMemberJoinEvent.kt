package net.perfectdreams.loritta.deviousfun.events.guild.member

import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.entities.Member
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.events.guild.GuildEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildMemberJoinEvent(
    jda: JDA,
    gateway: DeviousGateway,
    guild: Guild,
    val user: User,
    val member: Member
) : GuildEvent(jda, gateway, guild)