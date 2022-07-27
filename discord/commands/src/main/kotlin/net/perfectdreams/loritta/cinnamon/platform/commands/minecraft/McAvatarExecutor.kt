package net.perfectdreams.loritta.cinnamon.platform.commands.minecraft

import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McAvatarExecutor(loritta: LorittaCinnamon, mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    loritta,
    "avatars",
    mojang
)