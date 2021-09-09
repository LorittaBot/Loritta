package net.perfectdreams.loritta.cinnamon.commands.minecraft

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McHeadExecutor(mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    "renders/head",
    mojang
) {
    companion object : CommandExecutorDeclaration(McHeadExecutor::class) {
        override val options = CrafatarExecutorBase.options
    }
}