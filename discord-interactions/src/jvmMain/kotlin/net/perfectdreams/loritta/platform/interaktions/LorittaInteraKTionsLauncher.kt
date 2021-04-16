package net.perfectdreams.loritta.platform.interaktions

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.utils.ConfigUtils

fun main() {
    val loritta = LorittaInteraKTions(ConfigUtils.parseConfig(LorittaBot::class))
    loritta.start()
}