package net.perfectdreams.loritta.deviousfun.events.guild

import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildJoinEvent(deviousShard: DeviousShard, gateway: DeviousGateway, guild: Guild) :
    GuildEvent(deviousShard, gateway, guild)