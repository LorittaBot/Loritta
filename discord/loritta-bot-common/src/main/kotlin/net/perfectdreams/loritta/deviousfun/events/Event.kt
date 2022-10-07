package net.perfectdreams.loritta.deviousfun.events

import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

abstract class Event(
    val jda: JDA,
    val gateway: DeviousGateway
)