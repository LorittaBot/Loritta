package net.perfectdreams.loritta.deviousfun.events.guild

import dev.kord.gateway.GuildDelete
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildLeaveEvent(deviousShard: DeviousShard, gateway: DeviousGateway, guild: Guild, val event: GuildDelete) :
    GuildEvent(deviousShard, gateway, guild)