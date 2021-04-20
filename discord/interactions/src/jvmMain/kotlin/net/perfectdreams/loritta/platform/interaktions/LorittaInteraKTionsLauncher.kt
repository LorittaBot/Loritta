package net.perfectdreams.loritta.platform.interaktions

import net.perfectdreams.loritta.common.utils.ConfigUtils
import net.perfectdreams.loritta.discord.parseDiscordConfig

fun main() {
    val loritta = LorittaInteraKTions(ConfigUtils.parseConfig(), ConfigUtils.parseDiscordConfig())
    loritta.start()
}