package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.rest.json.response.BanResponse
import net.perfectdreams.loritta.deviousfun.DeviousFun

class Ban(
    val deviousFun: DeviousFun,
    val user: User,
    val ban: BanResponse,
) {
    val reason: String?
        get() = ban.reason
}