package net.perfectdreams.loritta.cinnamon.platform.commands.minecraft

import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McAvatarExecutor(mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    "avatars",
    mojang
) {
    companion object : SlashCommandExecutorDeclaration() {
        override val options = CrafatarExecutorBase.options
    }
}