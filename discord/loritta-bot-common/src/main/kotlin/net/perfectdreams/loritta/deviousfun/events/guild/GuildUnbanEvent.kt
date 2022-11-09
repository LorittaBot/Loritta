package net.perfectdreams.loritta.deviousfun.events.guild

import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildUnbanEvent(
    deviousFun: DeviousFun,
    gateway: DeviousGateway,
    guild: Guild,
    val user: User
) : GuildEvent(deviousFun, gateway, guild)