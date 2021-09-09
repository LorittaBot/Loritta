package net.perfectdreams.loritta.cinnamon.commands.minecraft

import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McBodyExecutor(emotes: Emotes, mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    "renders/body",
    emotes,
    mojang
) {
    companion object : CommandExecutorDeclaration(McBodyExecutor::class) {
        override val options = CrafatarExecutorBase.options
    }
}