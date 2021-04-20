package net.perfectdreams.loritta.platform.kord

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.utils.ConfigUtils
import net.perfectdreams.loritta.discord.parseDiscordConfig

fun main() {
    val loritta = LorittaKord(ConfigUtils.parseConfig(), ConfigUtils.parseDiscordConfig())
    loritta.start()
}