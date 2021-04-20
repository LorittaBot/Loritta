package net.perfectdreams.loritta.discord

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.utils.config.LorittaConfig

abstract class LorittaDiscord(
    config: LorittaConfig,
    val discordConfig: LorittaDiscordConfig
): LorittaBot(config)