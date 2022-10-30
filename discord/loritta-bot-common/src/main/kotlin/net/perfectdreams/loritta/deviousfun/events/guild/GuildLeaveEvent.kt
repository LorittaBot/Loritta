package net.perfectdreams.loritta.deviousfun.events.guild

import dev.kord.gateway.GuildDelete
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildLeaveEvent(deviousFun: DeviousFun, gateway: DeviousGateway, guild: Guild, val event: GuildDelete) :
    GuildEvent(deviousFun, gateway, guild)