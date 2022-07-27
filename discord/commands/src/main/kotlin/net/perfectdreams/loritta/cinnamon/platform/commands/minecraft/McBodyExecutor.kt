package net.perfectdreams.loritta.cinnamon.platform.commands.minecraft

import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McBodyExecutor(loritta: LorittaCinnamon, mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    loritta,
    "renders/body",
    mojang
)