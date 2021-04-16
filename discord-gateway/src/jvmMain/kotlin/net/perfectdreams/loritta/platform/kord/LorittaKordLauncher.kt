package net.perfectdreams.loritta.platform.kord

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.utils.ConfigUtils

fun main() {
    val loritta = LorittaKord(ConfigUtils.parseConfig(LorittaBot::class))
    loritta.start()
}