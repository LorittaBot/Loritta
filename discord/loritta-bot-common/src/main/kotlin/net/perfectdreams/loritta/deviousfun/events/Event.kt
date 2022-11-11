package net.perfectdreams.loritta.deviousfun.events

import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

abstract class Event(
    val deviousShard: DeviousShard,
    val gateway: DeviousGateway
)