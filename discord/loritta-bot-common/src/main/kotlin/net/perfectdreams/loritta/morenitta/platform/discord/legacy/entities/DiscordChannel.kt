package net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import net.perfectdreams.loritta.common.entities.Channel
import net.perfectdreams.loritta.common.entities.Member

open class DiscordChannel(@JsonIgnore val handle: net.perfectdreams.loritta.deviousfun.entities.Channel) : Channel {
    override val name: String
        get() = handle.name!!
}