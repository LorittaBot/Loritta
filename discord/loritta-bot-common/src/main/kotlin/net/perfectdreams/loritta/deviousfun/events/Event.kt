package net.perfectdreams.loritta.deviousfun.events

import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

abstract class Event(
    val deviousFun: DeviousFun,
    val gateway: DeviousGateway
)