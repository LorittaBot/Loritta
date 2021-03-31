package net.perfectdreams.loritta.commands.vanilla.minecraft

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.commands.vanilla.minecraft.declarations.MinecraftCommandDeclaration

class McAvatarCommand(m: LorittaBot) : CrafatarCommandBase(
    m,
    "avatars",
    MinecraftCommandDeclaration.Avatar
)