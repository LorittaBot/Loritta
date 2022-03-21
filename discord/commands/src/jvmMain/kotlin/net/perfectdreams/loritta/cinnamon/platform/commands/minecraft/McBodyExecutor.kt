package net.perfectdreams.loritta.cinnamon.platform.commands.minecraft

import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McBodyExecutor(mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    "renders/body",
    mojang
) {
    companion object : SlashCommandExecutorDeclaration(McBodyExecutor::class) {
        override val options = CrafatarExecutorBase.options
    }
}