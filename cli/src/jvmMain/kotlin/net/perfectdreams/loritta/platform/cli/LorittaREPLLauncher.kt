package net.perfectdreams.loritta.platform.cli

import net.perfectdreams.loritta.common.utils.ConfigUtils

suspend fun main() {
    val loritta = LorittaREPL(ConfigUtils.parseConfig())
    loritta.start()
}