package net.perfectdreams.loritta.platform.twitter

import net.perfectdreams.loritta.common.utils.ConfigUtils
import net.perfectdreams.loritta.platform.twitter.utils.config.parseTwitterConfig

object LorittaTwitterLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val loritta = LorittaTwitter(
            ConfigUtils.parseConfig(),
            ConfigUtils.parseTwitterConfig()
        )

        loritta.start()
    }
}