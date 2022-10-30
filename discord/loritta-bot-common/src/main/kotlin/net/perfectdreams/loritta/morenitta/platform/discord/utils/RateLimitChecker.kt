package net.perfectdreams.loritta.morenitta.platform.discord.utils

import net.perfectdreams.loritta.morenitta.LorittaBot

class RateLimitChecker(val m: LorittaBot) {
    fun checkIfRequestShouldBeIgnored() = false
}