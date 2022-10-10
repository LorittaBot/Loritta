package net.perfectdreams.loritta.deviousfun.events.guild

import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildReadyEvent(
    deviousFun: DeviousFun,
    gateway: DeviousGateway,
    guild: Guild
) : GuildEvent(deviousFun, gateway, guild)