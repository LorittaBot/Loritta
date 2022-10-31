package net.perfectdreams.loritta.deviousfun.events.guild

import dev.kord.gateway.GuildCreate
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildJoinEvent(deviousFun: DeviousFun, gateway: DeviousGateway, guild: Guild) :
    GuildEvent(deviousFun, gateway, guild)