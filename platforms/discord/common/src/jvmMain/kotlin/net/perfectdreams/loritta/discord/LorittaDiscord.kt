package net.perfectdreams.loritta.discord

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

abstract class LorittaDiscord(
    config: LorittaConfig,
    val discordConfig: LorittaDiscordConfig
): LorittaBot(config) {
    val mojangApi = MinecraftMojangAPI()
}