package net.perfectdreams.loritta.cinnamon.commands.minecraft

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McAvatarExecutor(mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    "avatars",
    mojang
) {
    companion object : CommandExecutorDeclaration(McAvatarExecutor::class) {
        override val options = CrafatarExecutorBase.options
    }
}