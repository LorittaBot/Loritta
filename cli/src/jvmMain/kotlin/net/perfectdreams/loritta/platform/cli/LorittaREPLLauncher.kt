package net.perfectdreams.loritta.platform.cli

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.utils.ConfigUtils

suspend fun main(args: Array<String>) {
    val loritta = LorittaREPL(ConfigUtils.parseConfig(LorittaBot::class))
    loritta.start()
}