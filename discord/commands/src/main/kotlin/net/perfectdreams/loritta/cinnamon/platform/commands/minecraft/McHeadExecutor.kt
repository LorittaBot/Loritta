package net.perfectdreams.loritta.cinnamon.platform.commands.minecraft

import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McHeadExecutor(mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    "renders/head",
    mojang
) {
    companion object : SlashCommandExecutorDeclaration() {
        override val options = CrafatarExecutorBase.options
    }
}