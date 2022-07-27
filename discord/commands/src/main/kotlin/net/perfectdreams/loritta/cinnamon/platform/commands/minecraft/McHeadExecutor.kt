package net.perfectdreams.loritta.cinnamon.platform.commands.minecraft

import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McHeadExecutor(loritta: LorittaCinnamon, mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    loritta,
    "renders/head",
    mojang
)