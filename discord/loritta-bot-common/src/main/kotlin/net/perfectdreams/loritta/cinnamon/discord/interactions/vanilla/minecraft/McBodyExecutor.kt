package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McBodyExecutor(loritta: LorittaBot, mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    loritta,
    "renders/body",
    mojang
)