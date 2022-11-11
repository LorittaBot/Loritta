package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.rest.json.response.BanResponse
import net.perfectdreams.loritta.deviousfun.DeviousShard

class Ban(
    val deviousShard: DeviousShard,
    val user: User,
    val ban: BanResponse,
) {
    val reason: String?
        get() = ban.reason
}