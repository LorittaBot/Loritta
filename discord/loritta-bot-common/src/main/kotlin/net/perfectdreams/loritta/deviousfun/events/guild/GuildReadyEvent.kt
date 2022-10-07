package net.perfectdreams.loritta.deviousfun.events.guild

import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildReadyEvent(
    jda: JDA,
    gateway: DeviousGateway,
    guild: Guild
) : GuildEvent(jda, gateway, guild)