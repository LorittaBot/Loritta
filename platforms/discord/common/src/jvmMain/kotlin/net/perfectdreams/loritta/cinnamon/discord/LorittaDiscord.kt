package net.perfectdreams.loritta.cinnamon.discord

import net.perfectdreams.loritta.cinnamon.common.LorittaBot
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

abstract class LorittaDiscord(
    config: LorittaConfig,
    val discordConfig: LorittaDiscordConfig
): LorittaBot(config) {
    val mojangApi = MinecraftMojangAPI()
}