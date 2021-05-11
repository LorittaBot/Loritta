package net.perfectdreams.loritta.commands.minecraft

import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McHeadExecutor(emotes: Emotes, mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    "renders/head",
    emotes,
    mojang
) {
    companion object : CommandExecutorDeclaration(McHeadExecutor::class) {
        override val options = CrafatarExecutorBase.options
    }
}