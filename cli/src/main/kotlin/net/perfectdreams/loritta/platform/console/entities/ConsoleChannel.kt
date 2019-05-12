package net.perfectdreams.loritta.platform.console.entities

import net.perfectdreams.loritta.api.entities.Channel
import net.perfectdreams.loritta.api.entities.Member

open class ConsoleChannel : Channel {
    override val name: String?
        get() = "ConsoleChannel"
    override val participants: List<Member>
        get() = listOf()
}