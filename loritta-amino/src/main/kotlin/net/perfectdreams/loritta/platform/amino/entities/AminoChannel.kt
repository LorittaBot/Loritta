package net.perfectdreams.loritta.platform.amino.entities

import net.perfectdreams.loritta.api.entities.Channel
import net.perfectdreams.loritta.api.entities.Member

open class AminoChannel : Channel {
    override val name: String?
        get() = "ConsoleChannel"
    override val participants: List<Member>
        get() = listOf()
}