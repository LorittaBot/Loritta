package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft

import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McAvatarExecutor(loritta: LorittaCinnamon, mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    loritta,
    "avatars",
    mojang
)