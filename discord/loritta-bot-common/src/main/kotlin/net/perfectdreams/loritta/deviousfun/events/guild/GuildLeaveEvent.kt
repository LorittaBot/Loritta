package net.perfectdreams.loritta.deviousfun.events.guild

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.GuildDelete
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class GuildLeaveEvent(jda: JDA, gateway: DeviousGateway, guild: Guild, val event: GuildDelete) : GuildEvent(jda, gateway, guild)