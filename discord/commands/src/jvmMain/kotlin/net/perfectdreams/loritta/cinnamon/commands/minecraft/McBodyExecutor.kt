package net.perfectdreams.loritta.cinnamon.commands.minecraft

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McBodyExecutor(mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    "renders/body",
    mojang
) {
    companion object : CommandExecutorDeclaration(McBodyExecutor::class) {
        override val options = CrafatarExecutorBase.options
    }
}